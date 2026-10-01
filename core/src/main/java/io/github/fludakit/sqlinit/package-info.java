/**
 * Database migration and schema initialization toolkit.
 *
 * <p>This package provides lightweight, versioned database migration support for applying
 * SQL scripts to a {@link javax.sql.DataSource}. The main entry point is
 * {@link io.github.fludakit.sqlinit.DbMigrator}, which tracks applied migrations in a
 * history table and ensures each migration runs exactly once.</p>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * DataSource dataSource = // ... your DataSource
 *
 * // Use defaults: scripts in classpath:/db/migration
 * DbMigrator migrator = new DbMigrator(dataSource);
 * migrator.migrate();
 *
 * // Or configure custom locations
 * SqlInitConfig config = SqlInitConfig.builder()
 *     .scriptLocations("classpath:/sql/migrations", "filesystem:/opt/migrations")
 *     .historyTable("schema_version")
 *     .build();
 * DbMigrator migrator = new DbMigrator(dataSource, config);
 * migrator.migrate();
 * }</pre>
 *
 * <h2>Migration Scripts</h2>
 * <p>Migration scripts must follow the naming convention {@code V<version>__<description>.sql},
 * where {@code <version>} is a numeric version and {@code <description>} is a human-readable name.
 * Examples:</p>
 * <ul>
 *   <li>{@code V1__create_users_table.sql}</li>
 *   <li>{@code V2__add_email_index.sql}</li>
 *   <li>{@code V10__rename_column.sql}</li>
 * </ul>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link io.github.fludakit.sqlinit.DbMigrator} — Main API for running migrations</li>
 *   <li>{@link io.github.fludakit.sqlinit.SqlInitConfig} — Configuration for script locations and history table</li>
 *   <li>{@link io.github.fludakit.sqlinit.Migration} — Represents a resolved migration script</li>
 *   <li>{@link io.github.fludakit.sqlinit.MigrationHistory} — Tracks applied migrations</li>
 *   <li>{@link io.github.fludakit.sqlinit.resource.ResourceResolver} — Resolves scripts from classpath, filesystem, or URLs</li>
 * </ul>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Versioned migrations with automatic ordering</li>
 *   <li>Idempotent execution: migrations run once and are tracked</li>
 *   <li>Atomic migration claiming via database locks</li>
 *   <li>Support for classpath, filesystem, and URL resources</li>
 *   <li>Custom resource resolvers via SPI</li>
 *   <li>Multi-database support (H2, PostgreSQL, MySQL, etc.)</li>
 * </ul>
 *
 * <h2>Migration History</h2>
 * <p>Applied migrations are tracked in a history table (default: {@code schema_migrations}).
 * Each row records the version, script name, status (running/succeeded/failed), and execution
 * timestamp. The table is created automatically on first use.</p>
 *
 * @see io.github.fludakit.sqlinit.DbMigrator
 * @see io.github.fludakit.sqlinit.SqlInitConfig
 * @see io.github.fludakit.sqlinit.Migration
 */
package io.github.fludakit.sqlinit;
