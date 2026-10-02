package io.github.fludakit.sqlinit.cdi;

import io.github.fludakit.sqlinit.resource.ResourceResolver;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WeldJunit5Extension.class)
class SqlInitCustomResolverTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator
            .from(SqlInitBootstrapper.class, MemoryResourceResolver.class)
            .build();

    @Inject
    SqlInitBootstrapper bootstrapper;

    @Test
    void registersCustomResolvers() throws IOException {
        ResourceResolver resolver = bootstrapper.resourceResolver();

        // memory: is registered, so resolving it dispatches to MemoryResourceResolver (empty) rather
        // than failing with "unknown protocol".
        assertTrue(resolver.getResources("memory:migrations").isEmpty());
    }
}
