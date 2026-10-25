package io.github.fludakit.sqlinit.version;

/**
 * Strategy for parsing and comparing migration versions.
 *
 * <p>Implementations define how version strings are extracted from migration filenames and how they
 * are ordered. This allows flexible versioning schemes beyond simple integers.</p>
 *
 * <p>Built-in implementations include {@link IntegerVersionStrategy}, {@link DottedVersionStrategy},
 * and {@link SemanticVersionStrategy}. Users can provide custom implementations for specialized
 * versioning needs.</p>
 */
public interface VersionStrategy {

    /**
     * Validates and normalizes a version string parsed from a migration filename.
     *
     * @param version the version part extracted from the filename (without the "V" prefix)
     * @return the normalized version string
     * @throws IllegalArgumentException if the version format is invalid
     */
    String parse(String version);

    /**
     * Compares two version strings for ordering.
     *
     * @param v1 the first version
     * @param v2 the second version
     * @return a negative integer, zero, or positive integer as v1 is less than, equal to, or
     *         greater than v2
     */
    int compare(String v1, String v2);

    /**
     * Returns the default version strategy (integer-based).
     */
    static VersionStrategy defaultStrategy() {
        return IntegerVersionStrategy.INSTANCE;
    }

    /**
     * Resolves a strategy from a configured name: {@code int}/{@code integer} (default),
     * {@code dotted}, {@code semantic}/{@code semver}, or the fully-qualified class name of a
     * custom implementation with a no-arg constructor. {@code null} or blank yields the default.
     *
     * @throws IllegalArgumentException if the name matches no built-in strategy and the class
     *                                  cannot be loaded, does not implement {@code VersionStrategy},
     *                                  or cannot be instantiated
     */
    static VersionStrategy fromName(String value) {
        if (value == null || value.isBlank()) {
            return defaultStrategy();
        }

        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (normalized) {
            case "int", "integer" -> IntegerVersionStrategy.INSTANCE;
            case "dotted" -> DottedVersionStrategy.INSTANCE;
            case "semantic", "semver" -> SemanticVersionStrategy.INSTANCE;
            default -> instantiate(value.trim());
        };
    }

    private static VersionStrategy instantiate(String className) {
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
