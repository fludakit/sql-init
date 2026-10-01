package io.github.fludakit.sqlinit.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DottedVersionStrategyTest {

    @Test
    void parsesValidVersions() {
        assertEquals("1", DottedVersionStrategy.INSTANCE.parse("1"));
        assertEquals("1.0", DottedVersionStrategy.INSTANCE.parse("1.0"));
        assertEquals("1.2.3", DottedVersionStrategy.INSTANCE.parse("1.2.3"));
        assertEquals("10.20.30", DottedVersionStrategy.INSTANCE.parse("10.20.30"));
    }

    @Test
    void comparesCorrectly() {
        assertTrue(DottedVersionStrategy.INSTANCE.compare("1", "2") < 0);
        assertTrue(DottedVersionStrategy.INSTANCE.compare("1.0", "1.1") < 0);
        assertTrue(DottedVersionStrategy.INSTANCE.compare("1.2", "2.0") < 0);
        assertTrue(DottedVersionStrategy.INSTANCE.compare("1.2.3", "1.2.4") < 0);
        assertTrue(DottedVersionStrategy.INSTANCE.compare("1.9", "2.0") < 0);
        assertEquals(0, DottedVersionStrategy.INSTANCE.compare("1", "1.0"));
        assertEquals(0, DottedVersionStrategy.INSTANCE.compare("1.0", "1.0.0"));
    }

    @Test
    void rejectsInvalidVersions() {
        assertThrows(IllegalArgumentException.class, () -> DottedVersionStrategy.INSTANCE.parse(""));
        assertThrows(IllegalArgumentException.class, () -> DottedVersionStrategy.INSTANCE.parse(null));
        assertThrows(IllegalArgumentException.class, () -> DottedVersionStrategy.INSTANCE.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> DottedVersionStrategy.INSTANCE.parse("1.2.3.4"));
        assertThrows(IllegalArgumentException.class, () -> DottedVersionStrategy.INSTANCE.parse("1.-1"));
    }
}
