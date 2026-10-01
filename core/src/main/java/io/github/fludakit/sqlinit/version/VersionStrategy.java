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
}
