package io.github.fludakit.sqlinit.cdi;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import jakarta.inject.Inject;

import static io.github.fludakit.sqlinit.cdi.TestDatabases.query;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(WeldJunit5Extension.class)
class SqlInitBootstrapperTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator
            .from(SqlInitBootstrapper.class, FallbackDataSourceProducer.class)
            .build();

    @Inject
    DataSource dataSource;

    @Test
    void fallsBackToTheDefaultDataSource() throws SQLException {
        assertEquals(List.of("Ada", "Grace"), query(dataSource, "SELECT name FROM engineers ORDER BY id"));
    }
}
