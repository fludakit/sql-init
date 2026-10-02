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
class SqlInitQualifiedDataSourceTest {

    @WeldSetup
    WeldInitiator setup = WeldInitiator
            .from(SqlInitBootstrapper.class, PreferredDataSourceProducer.class)
            .build();

    @Inject
    DataSource dataSource;

    @Inject
    @SqlInit
    DataSource sqlInitDataSource;

    @Test
    void prefersTheQualifiedDataSource() throws SQLException {
        assertEquals(List.of("Ada", "Grace"), query(sqlInitDataSource, "SELECT name FROM engineers ORDER BY id"));
        assertEquals(List.of("0"), query(dataSource,
                "SELECT CAST(COUNT(*) AS VARCHAR) FROM information_schema.tables WHERE table_name = 'ENGINEERS'"));
    }
}
