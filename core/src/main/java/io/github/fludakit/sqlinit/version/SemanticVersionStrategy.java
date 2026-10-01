package io.github.fludakit.sqlinit.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version strategy for semantic versions: V1.2.3, V1.0.0-alpha, V2.1.0-beta.1, etc.
 *
 * <p>Follows a simplified semantic versioning scheme: major.minor.patch[-prerelease]. Versions are
 * compared by major, minor, and patch first, then by prerelease identifier (release versions have
 * higher precedence than prerelease versions).</p>
 */
public final class SemanticVersionStrategy implements VersionStrategy {

    public static final SemanticVersionStrategy INSTANCE = new SemanticVersionStrategy();
    private static final Pattern SEMVER = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([\\w.]+))?");

    private SemanticVersionStrategy() {
    }

    @Override
    public String parse(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }

        Matcher matcher = SEMVER.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid semantic version (expected major.minor.patch[-prerelease]): " + version);
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        String prerelease = matcher.group(4);

        if (prerelease != null) {
            return major + "." + minor + "." + patch + "-" + prerelease;
        }
        return major + "." + minor + "." + patch;
    }

    @Override
    public int compare(String v1, String v2) {
        ParsedSemver p1 = parseSemver(v1);
        ParsedSemver p2 = parseSemver(v2);

        int cmp = Integer.compare(p1.major, p2.major);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(p1.minor, p2.minor);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(p1.patch, p2.patch);
        if (cmp != 0) {
            return cmp;
        }

        if (p1.prerelease == null && p2.prerelease == null) {
            return 0;
        }
        if (p1.prerelease == null) {
            return 1;
        }
        if (p2.prerelease == null) {
            return -1;
        }
        return p1.prerelease.compareTo(p2.prerelease);
    }

    private ParsedSemver parseSemver(String version) {
        Matcher matcher = SEMVER.matcher(version);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid semantic version: " + version);
        }
        return new ParsedSemver(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(4));
    }

    private record ParsedSemver(int major, int minor, int patch, String prerelease) {
    }
}
