package io.github.fludakit.sqlinit.config;

import io.github.fludakit.sqlinit.SqlInitConfig;
import io.github.fludakit.sqlinit.version.VersionStrategy;
import org.eclipse.microprofile.config.inject.ConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

/**
 * Produces the {@code @ApplicationScoped} {@link SqlInitConfig} bean from the
 * {@code fluda.sqlinit.*} MicroProfile Config properties.
 */
@ApplicationScoped
public class SqlInitConfigProducer {

    @Inject
    @ConfigProperties
    private SqlInitProperties properties;

    @Produces
    @ApplicationScoped
    public SqlInitConfig produce() {
        SqlInitConfig.Builder builder = SqlInitConfig.builder()
                .scriptLocations(properties.scriptLocations())
                .separator(properties.separator())
                .versionStrategy(VersionStrategy.fromName(properties.versionStrategy()));
        String dbType = properties.dbType();
        if (dbType != null && !dbType.isBlank()) {
            builder.dbType(dbType);
        }
        return builder.build();
    }
}
