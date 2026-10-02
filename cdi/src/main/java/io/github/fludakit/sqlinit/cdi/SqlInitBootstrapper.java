package io.github.fludakit.sqlinit.cdi;

import io.github.fludakit.sqlinit.DbMigrator;
import io.github.fludakit.sqlinit.SqlInitConfig;
import io.github.fludakit.sqlinit.resource.ResourceResolver;
import io.github.fludakit.sqlinit.resource.ResourceResolverRegistry;

import java.util.logging.Logger;
import javax.sql.DataSource;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.Interceptor;

/**
 * Applies the configured SQL migrations when the application starts.
 *
 * <p>A plain CDI bean, not a portable extension: it observes the container's {@link Startup} event
 * and runs the migrations. The {@link DataSource} is resolved by preferring one qualified with
 * {@link SqlInit} and falling back to the default unqualified bean. Any user-provided
 * {@link ResourceResolver} bean whose {@link ResourceResolver#protocol()} is set is registered so
 * migrations can be loaded from additional protocols.</p>
 */
@ApplicationScoped
public class SqlInitBootstrapper {

    private static final Logger LOGGER = Logger.getLogger(SqlInitBootstrapper.class.getName());

    public void onStartup(@Observes @Priority(Interceptor.Priority.APPLICATION + 500) Startup event) {
        DataSource ds = resolveDataSource();
        if (ds == null) {
            LOGGER.warning("No DataSource bean found, skipping SQL script initialization");
            return;
        }
        var configHandle = CDI.current().select(SqlInitConfig.class);
        SqlInitConfig config = configHandle.isResolvable() ? configHandle.get() : SqlInitConfig.defaults();
        new DbMigrator(ds, config, resourceResolver()).migrate();
    }

    private DataSource resolveDataSource() {
        var qualifiedHandle = CDI.current().select(DataSource.class, SqlInit.Literal.INSTANCE);
        if (qualifiedHandle.isResolvable()) {
            LOGGER.fine("Initializing the @SqlInit qualified DataSource");
            return qualifiedHandle.get();
        }
        var defaultHandle = CDI.current().select(DataSource.class);
        return defaultHandle.isResolvable() ? defaultHandle.get() : null;
    }

    ResourceResolver resourceResolver() {
        ResourceResolverRegistry registry = new ResourceResolverRegistry();
        for (var handle : CDI.current().select(ResourceResolver.class).handles()) {
            ResourceResolver resolver = handle.get();
            if (resolver.protocol() != null) {
                registry.register(resolver.protocol(), resolver);
            }
        }
        return registry;
    }
}
