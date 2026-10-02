package io.github.fludakit.sqlinit.config;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Holds the {@code fluda.sqlinit.*} MicroProfile Config properties for SQL migration initialization.
 */
@ApplicationScoped
@ConfigProperties(prefix = "fluda.sqlinit")
public class SqlInitProperties {

    @ConfigProperty(name = "script-locations")
    private List<String> scriptLocations = List.of("classpath:db/migration");

    private String separator = ";";

    @ConfigProperty(name = "db-type")
    private String dbType;

    @ConfigProperty(name = "version-strategy")
    private String versionStrategy = "integer";

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
