package io.github.fludakit.sqlinit.config;

import io.github.fludakit.sqlinit.SqlInitConfig;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.Dependent;
import java.util.List;

/**
 * Holds the {@code fluda.sqlinit.*} MicroProfile Config properties for SQL migration initialization.
 */
@Dependent
@ConfigProperties(prefix = "fluda.sqlinit")
public class SqlInitProperties {

    @ConfigProperty(name = "script-locations", defaultValue = SqlInitConfig.DEFAULT_SCRIPT_LOCATION)
    private List<String> scriptLocations;

    @ConfigProperty(name = "separator", defaultValue = SqlInitConfig.DEFAULT_SEPARATOR)
    private String separator;

    @ConfigProperty(name = "db-type")
    private String dbType;

    @ConfigProperty(name = "version-strategy", defaultValue = "integer")
    private String versionStrategy;

    public List<String> scriptLocations() {
        return scriptLocations;
    }

    public String separator() {
        return separator;
    }

    public String dbType() {
        return dbType;
    }

    public String versionStrategy() {
        return versionStrategy;
    }
}
