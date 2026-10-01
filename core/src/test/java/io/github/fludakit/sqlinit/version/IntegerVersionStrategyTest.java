package io.github.fludakit.sqlinit.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegerVersionStrategyTest {

    @Test
    void parsesValidVersions() {
        assertEquals("1", IntegerVersionStrategy.INSTANCE.parse("1"));
        assertEquals("10", IntegerVersionStrategy.INSTANCE.parse("10"));
        assertEquals("100", IntegerVersionStrategy.INSTANCE.parse("100"));
    }

    @Test
    void comparesCorrectly() {
        assertTrue(IntegerVersionStrategy.INSTANCE.compare("1", "2") < 0);
        assertTrue(IntegerVersionStrategy.INSTANCE.compare("10", "2") > 0);
        assertEquals(0, IntegerVersionStrategy.INSTANCE.compare("5", "5"));
    }

    @Test
    void rejectsInvalidVersions() {
        assertThrows(IllegalArgumentException.class, () -> IntegerVersionStrategy.INSTANCE.parse(""));
        assertThrows(IllegalArgumentException.class, () -> IntegerVersionStrategy.INSTANCE.parse(null));
        assertThrows(IllegalArgumentException.class, () -> IntegerVersionStrategy.INSTANCE.parse("abc"));
        assertThrows(IllegalArgumentException.class, () -> IntegerVersionStrategy.INSTANCE.parse("1.2"));
        assertThrows(IllegalArgumentException.class, () -> IntegerVersionStrategy.INSTANCE.parse("0"));
        assertThrows(IllegalArgumentException.class, () -> IntegerVersionStrategy.INSTANCE.parse("-1"));
    }
}
