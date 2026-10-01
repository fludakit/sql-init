package io.github.fludakit.sqlinit.config;

import io.github.fludakit.sqlinit.SqlInitConfig;
import io.smallrye.config.inject.ConfigExtension;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({WeldJunit5Extension.class})
class SqlInitConfigProducerWeldTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
                    // extensions
                    ConfigExtension.class,

                    // bean classes
                    SqlInitConfigProducer.class, SqlInitProperties.class
            )
            .build();

    @Inject
    SqlInitConfig config;

    @Test
    void mapsConfigProperties() {
        assertNotNull(config);
        assertEquals(List.of("classpath:db/migration", "filesystem:/opt/sql"), config.scriptLocations());
        assertEquals("/", config.separator());
        assertEquals("h2", config.dbType());
    }
}
