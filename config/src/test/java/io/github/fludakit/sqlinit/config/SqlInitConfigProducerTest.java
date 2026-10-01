package io.github.fludakit.sqlinit.config;

import io.github.fludakit.sqlinit.SqlInitConfig;
import io.github.fludakit.sqlinit.version.DottedVersionStrategy;
import io.github.fludakit.sqlinit.version.IntegerVersionStrategy;
import io.github.fludakit.sqlinit.version.SemanticVersionStrategy;
import io.github.fludakit.sqlinit.version.VersionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlInitConfigProducerTest {

    private SqlInitConfigProducer producer;
    private SqlInitProperties properties;

    @BeforeEach
    void setUp() {
        producer = new SqlInitConfigProducer();
        properties = new SqlInitProperties();
        setField(producer, "properties", properties);
        setField(properties, "scriptLocations", List.of("classpath:db/migration"));
        setField(properties, "separator", ";");
        setField(properties, "dbType", "");
    }

    @Test
    void resolvesIntegerStrategy() {
        setField(properties, "versionStrategy", "integer");
        SqlInitConfig config = producer.produce();
        assertInstanceOf(IntegerVersionStrategy.class, config.versionStrategy());
    }

    @Test
    void resolvesIntAlias() {
        setField(properties, "versionStrategy", "int");
        SqlInitConfig config = producer.produce();
        assertInstanceOf(IntegerVersionStrategy.class, config.versionStrategy());
    }

    @Test
    void resolvesDottedStrategy() {
        setField(properties, "versionStrategy", "dotted");
        SqlInitConfig config = producer.produce();
        assertInstanceOf(DottedVersionStrategy.class, config.versionStrategy());
    }

    @Test
    void resolvesSemanticStrategy() {
        setField(properties, "versionStrategy", "semantic");
        SqlInitConfig config = producer.produce();
        assertInstanceOf(SemanticVersionStrategy.class, config.versionStrategy());
    }

    @Test
    void resolvesSemverAlias() {
        setField(properties, "versionStrategy", "semver");
        SqlInitConfig config = producer.produce();
        assertInstanceOf(SemanticVersionStrategy.class, config.versionStrategy());
    }

    @Test
    void resolvesCustomStrategy() {
        setField(properties, "versionStrategy", TestVersionStrategy.class.getName());
        SqlInitConfig config = producer.produce();
        assertInstanceOf(TestVersionStrategy.class, config.versionStrategy());
    }

    @Test
    void rejectsInvalidCustomStrategy() {
        setField(properties, "versionStrategy", "java.lang.String");
        assertThrows(IllegalArgumentException.class, producer::produce);
    }

    @Test
    void rejectsNonExistentClass() {
        setField(properties, "versionStrategy", "com.example.NonExistent");
        assertThrows(IllegalArgumentException.class, producer::produce);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static class TestVersionStrategy implements VersionStrategy {
        @Override
        public String parse(String version) {
            return version;
        }

        @Override
        public int compare(String v1, String v2) {
            return v1.compareTo(v2);
        }
    }
}
