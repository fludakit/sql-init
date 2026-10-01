package io.github.fludakit.sqlinit.version;

/**
 * Version strategy for simple integer versions: V1, V2, V3, etc.
 *
 * <p>This is the default strategy and maintains backward compatibility with existing migrations.
 * Versions are stored as their string representation but compared as integers.</p>
 */
public final class IntegerVersionStrategy implements VersionStrategy {

    public static final IntegerVersionStrategy INSTANCE = new IntegerVersionStrategy();

    private IntegerVersionStrategy() {
    }

    @Override
    public String parse(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        try {
            int parsed = Integer.parseInt(version);
            if (parsed < 1) {
                throw new IllegalArgumentException("Version must be positive: " + version);
            }
            return String.valueOf(parsed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer version: " + version, e);
        }
    }

    @Override
    public int compare(String v1, String v2) {
        return Integer.compare(Integer.parseInt(v1), Integer.parseInt(v2));
    }
}
