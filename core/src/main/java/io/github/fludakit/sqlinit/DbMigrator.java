package io.github.fludakit.sqlinit;

import io.github.fludakit.sqlinit.resource.Resource;
import io.github.fludakit.sqlinit.resource.ResourceResolver;
import io.github.fludakit.sqlinit.resource.ResourceResolverRegistry;
import io.github.fludakit.sqlinit.version.VersionStrategy;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

/**
 * Applies versioned SQL migrations to a {@link DataSource}, tracking them in a history table.
 *
 * <p>Scripts are resolved from the configured {@link SqlInitConfig#scriptLocations()} and must be
 * named {@code V<version>__<description>.sql}. Each pending migration is claimed by inserting a
 * {@code running} row (atomic via the {@code version} primary key), executed in its own
 * transaction, then marked {@code succeeded} or {@code failed}.</p>
 */
public final class DbMigrator {

    private static final Logger LOGGER = Logger.getLogger(DbMigrator.class.getName());
    private static final Pattern MIGRATION_NAME = Pattern.compile("V([^_]+)__(.+)\\.sql");

    private final DataSource dataSource;
    private final SqlInitConfig config;
    private final ResourceResolver resourceResolver;

    public DbMigrator(DataSource dataSource) {
        this(dataSource, SqlInitConfig.defaults(), new ResourceResolverRegistry());
    }

    public DbMigrator(DataSource dataSource, SqlInitConfig config) {
        this(dataSource, config, new ResourceResolverRegistry());
    }

    public DbMigrator(DataSource dataSource, SqlInitConfig config, ResourceResolver resourceResolver) {
        this.dataSource = dataSource;
        this.config = config;
        this.resourceResolver = resourceResolver;
    }

    /**
     * Resolves the configured scripts and applies the migrations that have not run yet.
     *
     * @throws SqlInitException if any error occurs during migration
     */
    public void migrate() {
        try {
            List<Migration> migrations = resolve();
            if (migrations.isEmpty()) {
                LOGGER.info("No SQL migrations resolved, skipping database migration");
                return;
            }

            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(true);
                MigrationHistory history = new MigrationHistory(connection, dbType(connection));
                history.ensureTable();

                Map<String, MigrationHistory.Status> applied = history.applied();
                int appliedCount = 0;
                for (Migration migration : migrations) {
                    MigrationHistory.Status status = applied.get(migration.version());
                    if (status == MigrationHistory.Status.SUCCEEDED) {
                        LOGGER.fine(() -> "Skipping already applied migration: " + migration.script());
                        continue;
                    }
                    runMigration(connection, history, migration, status != null);
                    appliedCount++;
                }
                int count = appliedCount;
                LOGGER.info(() -> "Database migration completed, applied " + count + " migration(s)");
            }
        } catch (SqlInitException e) {
            throw e;
        } catch (Exception e) {
            throw new SqlInitException("Database migration failed", e);
        }
    }

    private List<Migration> resolve() {
        Map<String, Resource> byFilename = new LinkedHashMap<>();
        for (String location : config.scriptLocations()) {
            List<Resource> found = resourceResolver.getResources(location);
            for (Resource resource : found) {
                byFilename.putIfAbsent(resource.getFilename(), resource);
            }
        }
        List<Resource> resources = new ArrayList<>(byFilename.values());

        VersionStrategy strategy = config.versionStrategy();
        List<Migration> migrations = new ArrayList<>();
        for (Resource resource : resources) {
            Matcher matcher = MIGRATION_NAME.matcher(resource.getFilename());
            if (!matcher.matches()) {
                LOGGER.warning(() -> "Ignoring resource that does not match V<version>__<description>.sql: "
                        + resource.getFilename());
                continue;
            }
            String version = strategy.parse(matcher.group(1));
            String description = matcher.group(2);
            migrations.add(new Migration(version, description, resource));
        }
        migrations.sort((m1, m2) -> strategy.compare(m1.version(), m2.version()));
        for (int i = 1; i < migrations.size(); i++) {
            if (migrations.get(i).version().equals(migrations.get(i - 1).version())) {
                throw new SqlInitException("Duplicate migration version " + migrations.get(i).version()
                        + ": " + migrations.get(i - 1).script() + " and " + migrations.get(i).script());
            }
        }
        return migrations;
    }

    private DbType dbType(Connection connection) {
        if (config.dbType() != null) {
            return DbType.fromName(config.dbType());
        }
        try {
            return DbType.detect(connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getURL());
        } catch (SQLException e) {
            throw new SqlInitException("Failed to detect database type", e);
        }
    }

    private void runMigration(Connection connection, MigrationHistory history, Migration migration, boolean retry) {
        LOGGER.info(() -> "Applying migration: " + migration.script());

        try {
            connection.setAutoCommit(true);
            if (retry) {
                history.markRunning(migration.version());
            } else {
                history.insertRunning(migration);
            }

            connection.setAutoCommit(false);
            try {
                executeScript(connection, migration);
                connection.commit();
            } catch (SqlInitException e) {
                rollbackQuietly(connection);
                connection.setAutoCommit(true);
                markFailed(history, migration, e);
                throw e;
            } catch (Exception e) {
                rollbackQuietly(connection);
                connection.setAutoCommit(true);
                SqlInitException wrapped = new SqlInitException("Failed to execute SQL script: " + migration.script(), e);
                markFailed(history, migration, wrapped);
                throw wrapped;
            }

            connection.setAutoCommit(true);
            history.markSucceeded(migration.version());
        } catch (SqlInitException e) {
            throw e;
        } catch (SQLException e) {
            throw new SqlInitException("Migration transaction failed: " + migration.script(), e);
        }
    }

    private void markFailed(MigrationHistory history, Migration migration, Exception failure) {
        try {
            history.markFailed(migration.version(), truncate(failure.getMessage()));
        } catch (Exception markFailure) {
            failure.addSuppressed(markFailure);
        }
    }

    private void executeScript(Connection connection, Migration migration) {
        try (InputStream in = migration.resource().getInputStream()) {
            List<String> statements = SqlScriptParser.parse(in, config.separator());
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
            }
        } catch (SqlInitException e) {
            throw e;
        } catch (Exception e) {
            throw new SqlInitException("Failed to execute SQL script: " + migration.script(), e);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            // ignored
        }
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
