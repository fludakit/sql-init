package io.github.fludakit.sqlinit;

import java.util.List;
import java.util.Locale;

/**
 * The database the {@code db_migrations} history table is created for.
 *
 * <p>Each member points at the classpath resource holding its dialect-specific
 * {@code CREATE TABLE db_migrations} statement and carries its own accepted name aliases,
 * JDBC URL prefixes, and product-name markers, so adding a database is a one-constant change.</p>
 */
public enum DbType {

    H2("sqlinit/init-h2.sql",
            List.of("h2"), List.of("jdbc:h2:"), List.of("h2")),
    POSTGRESQL("sqlinit/init-postgresql.sql",
            List.of("pg", "postgres", "postgresql"), List.of("jdbc:postgresql:"), List.of("postgres")),
    MYSQL("sqlinit/init-mysql.sql",
            List.of("mysql", "mariadb"), List.of("jdbc:mysql:", "jdbc:mariadb:"), List.of("mysql", "mariadb")),
    MSSQL("sqlinit/init-mssql.sql",
            List.of("mssql", "sqlserver", "sql_server"), List.of("jdbc:sqlserver:"), List.of("microsoft", "sql server")),
    ORACLE("sqlinit/init-oracle.sql",
            List.of("oracle"), List.of("jdbc:oracle:"), List.of("oracle"));

    private final String initSqlResource;
    private final List<String> names;
    private final List<String> urlPrefixes;
    private final List<String> productNameMarkers;

    DbType(String initSqlResource, List<String> names, List<String> urlPrefixes, List<String> productNameMarkers) {
        this.initSqlResource = initSqlResource;
        this.names = names;
        this.urlPrefixes = urlPrefixes;
        this.productNameMarkers = productNameMarkers;
    }

    /** The classpath resource holding this database's history-table initialization statement. */
    public String initSqlResource() {
        return initSqlResource;
    }

    /**
     * Resolves a configured database type name, accepting the member's aliases (case-insensitive).
     *
     * @throws IllegalArgumentException if the name is not a known database type
     */
    public static DbType fromName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (DbType type : values()) {
            if (type.names.contains(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown database type: " + name);
    }

    /**
     * Detects the database type from the JDBC product name and, failing that, the JDBC URL.
     *
     * @throws SqlInitException if the type cannot be determined
     */
    public static DbType detect(String productName, String jdbcUrl) {
        DbType detected = detectByProductName(productName);
        if (detected == null && jdbcUrl != null) {
            detected = detectByJdbcUrl(jdbcUrl);
        }
        if (detected == null) {
            throw new SqlInitException("Cannot detect the database type from product name '"
                    + productName + "' and JDBC URL '" + jdbcUrl
                    + "'; set fluda.sqlinit.db-type explicitly");
        }
        return detected;
    }

    private static DbType detectByProductName(String productName) {
        if (productName == null) {
            return null;
        }
        String name = productName.toLowerCase(Locale.ROOT);
        for (DbType type : values()) {
            if (type.productNameMarkers.stream().anyMatch(name::contains)) {
                return type;
            }
        }
        return null;
    }

    private static DbType detectByJdbcUrl(String jdbcUrl) {
        String url = jdbcUrl.toLowerCase(Locale.ROOT);
        for (DbType type : values()) {
            if (type.urlPrefixes.stream().anyMatch(url::startsWith)) {
                return type;
            }
        }
        return null;
    }
}
