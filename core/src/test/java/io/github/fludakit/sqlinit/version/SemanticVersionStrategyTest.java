package io.github.fludakit.sqlinit.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticVersionStrategyTest {

    @Test
    void parsesValidVersions() {
        assertEquals("1.0.0", SemanticVersionStrategy.INSTANCE.parse("1.0.0"));
        assertEquals("1.2.3", SemanticVersionStrategy.INSTANCE.parse("1.2.3"));
        assertEquals("1.0.0-alpha", SemanticVersionStrategy.INSTANCE.parse("1.0.0-alpha"));
        assertEquals("2.1.0-beta.1", SemanticVersionStrategy.INSTANCE.parse("2.1.0-beta.1"));
    }

    @Test
    void comparesCorrectly() {
        assertTrue(SemanticVersionStrategy.INSTANCE.compare("1.0.0", "2.0.0") < 0);
        assertTrue(SemanticVersionStrategy.INSTANCE.compare("1.1.0", "1.2.0") < 0);
        assertTrue(SemanticVersionStrategy.INSTANCE.compare("1.0.1", "1.0.2") < 0);
        assertTrue(SemanticVersionStrategy.INSTANCE.compare("1.0.0-alpha", "1.0.0") < 0);
        assertTrue(SemanticVersionStrategy.INSTANCE.compare("1.0.0-alpha", "1.0.0-beta") < 0);
        assertEquals(0, SemanticVersionStrategy.INSTANCE.compare("1.0.0", "1.0.0"));
    }

    @Test
    void rejectsInvalidVersions() {
        assertThrows(IllegalArgumentException.class, () -> SemanticVersionStrategy.INSTANCE.parse(""));
        assertThrows(IllegalArgumentException.class, () -> SemanticVersionStrategy.INSTANCE.parse(null));
        assertThrows(IllegalArgumentException.class, () -> SemanticVersionStrategy.INSTANCE.parse("1"));
        assertThrows(IllegalArgumentException.class, () -> SemanticVersionStrategy.INSTANCE.parse("1.2"));
        assertThrows(IllegalArgumentException.class, () -> SemanticVersionStrategy.INSTANCE.parse("abc"));
    }
}
