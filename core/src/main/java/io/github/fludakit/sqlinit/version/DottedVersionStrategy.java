package io.github.fludakit.sqlinit.version;

/**
 * Version strategy for dotted versions: V1.0, V1.2.3, etc.
 *
 * <p>Versions are split on dots and compared component by component. Missing components are treated
 * as zero, so "1" equals "1.0" equals "1.0.0".</p>
 */
public final class DottedVersionStrategy implements VersionStrategy {

    public static final DottedVersionStrategy INSTANCE = new DottedVersionStrategy();

    private DottedVersionStrategy() {
    }

    @Override
    public String parse(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        String[] parts = version.split("\\.");
        if (parts.length > 3) {
            throw new IllegalArgumentException("Version has too many components: " + version);
        }

        int[] components = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                components[i] = Integer.parseInt(parts[i]);
                if (components[i] < 0) {
                    throw new IllegalArgumentException("Version components must be non-negative: " + version);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid version component: " + parts[i], e);
            }
        }

        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                normalized.append('.');
            }
            normalized.append(components[i]);
        }
        return normalized.toString();
    }

    @Override
    public int compare(String v1, String v2) {
        int[] parts1 = parseComponents(v1);
        int[] parts2 = parseComponents(v2);

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int p1 = i < parts1.length ? parts1[i] : 0;
            int p2 = i < parts2.length ? parts2[i] : 0;
            int cmp = Integer.compare(p1, p2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    private int[] parseComponents(String version) {
        String[] parts = version.split("\\.");
        int[] components = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            components[i] = Integer.parseInt(parts[i]);
        }
        return components;
    }
}
