package io.github.fludakit.sqlinit;

import io.github.fludakit.sqlinit.version.VersionStrategy;

import java.util.List;

/**
 * Immutable configuration for SQL migration initialization.
 */
public class SqlInitConfig {

    public static final String DEFAULT_SEPARATOR = ";";
    public static final String DEFAULT_SCRIPT_LOCATION = "classpath:db/migration";

    private final List<String> scriptLocations;
    private final String separator;
    private final String dbType;
    private final VersionStrategy versionStrategy;

    /**
     * No-arg constructor required for CDI client proxies.
     */
    public SqlInitConfig() {
        this.scriptLocations = List.of(DEFAULT_SCRIPT_LOCATION);
        this.separator = DEFAULT_SEPARATOR;
        this.dbType = null;
        this.versionStrategy = VersionStrategy.defaultStrategy();
    }

    private SqlInitConfig(Builder builder) {
        this.scriptLocations = List.copyOf(builder.scriptLocations);
        this.separator = builder.separator;
        this.dbType = builder.dbType;
        this.versionStrategy = builder.versionStrategy != null ? builder.versionStrategy : VersionStrategy.defaultStrategy();
    }

    public static SqlInitConfig defaults() {
        return new Builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** The script locations to scan for versioned migrations (default {@code classpath:db/migration}). */
    public List<String> scriptLocations() {
        return scriptLocations;
    }

    /** The statement separator passed to the script parser (default {@code ;}). */
    public String separator() {
        return separator;
    }

    /** The configured database type, or {@code null} to auto-detect it from the connection. */
    public String dbType() {
        return dbType;
    }

    /** The version strategy for parsing and comparing migration versions (default integer-based). */
    public VersionStrategy versionStrategy() {
        return versionStrategy;
    }

    public static final class Builder {

        private List<String> scriptLocations = List.of(DEFAULT_SCRIPT_LOCATION);
        private String separator = DEFAULT_SEPARATOR;
        private String dbType;
        private VersionStrategy versionStrategy;

        public Builder scriptLocations(List<String> scriptLocations) {
            this.scriptLocations = scriptLocations;
            return this;
        }

        public Builder separator(String separator) {
            this.separator = separator;
            return this;
        }

        public Builder dbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public Builder versionStrategy(VersionStrategy versionStrategy) {
            this.versionStrategy = versionStrategy;
            return this;
        }

        public SqlInitConfig build() {
            return new SqlInitConfig(this);
        }
    }
}
