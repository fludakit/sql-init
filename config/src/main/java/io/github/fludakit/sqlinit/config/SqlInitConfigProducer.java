package io.github.fludakit.sqlinit.config;

import io.github.fludakit.sqlinit.SqlInitConfig;
import io.github.fludakit.sqlinit.version.DottedVersionStrategy;
import io.github.fludakit.sqlinit.version.IntegerVersionStrategy;
import io.github.fludakit.sqlinit.version.SemanticVersionStrategy;
import io.github.fludakit.sqlinit.version.VersionStrategy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperties;

/**
 * Produces the {@code @ApplicationScoped} {@link SqlInitConfig} bean from the
 * {@code fluda.sqlinit.*} MicroProfile Config properties.
 */
@ApplicationScoped
public class SqlInitConfigProducer {

    @Inject @ConfigProperties
    private SqlInitProperties properties;

    @Produces
    @ApplicationScoped
    public SqlInitConfig produce() {
        SqlInitConfig.Builder builder = SqlInitConfig.builder()
                .scriptLocations(properties.scriptLocations())
                .separator(properties.separator())
                .versionStrategy(resolveVersionStrategy(properties.versionStrategy()));
        String dbType = properties.dbType();
        if (dbType != null && !dbType.isBlank()) {
            builder.dbType(dbType);
        }
        return builder.build();
    }

    private VersionStrategy resolveVersionStrategy(String value) {
        if (value == null || value.isBlank()) {
            return VersionStrategy.defaultStrategy();
        }

        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "int", "integer" -> IntegerVersionStrategy.INSTANCE;
            case "dotted" -> DottedVersionStrategy.INSTANCE;
            case "semantic", "semver" -> SemanticVersionStrategy.INSTANCE;
            default -> instantiateCustomStrategy(value.trim());
        };
    }

    private VersionStrategy instantiateCustomStrategy(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (!VersionStrategy.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(
                        "Class " + className + " does not implement VersionStrategy");
            }
            return (VersionStrategy) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Failed to instantiate custom VersionStrategy: " + className, e);
        }
    }
}
