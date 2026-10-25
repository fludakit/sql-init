package io.github.fludakit.sqlinit.resource;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

final class ResourceUtils {

    /** Pattern matching every file below the location root. */
    static final String ALL_FILES = "**/*";

    private ResourceUtils() {
    }

    static ClassLoader defaultClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader != null ? classLoader : ResourceUtils.class.getClassLoader();
    }

    static String stripLeadingSlash(String location) {
        String result = location;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    /**
     * Returns the end index of the wildcard-free root portion of a pattern — the prefix up to (but
     * excluding) the first segment containing wildcards. When the pattern starts with a wildcard
     * segment the root is empty and this returns {@code 0}; when no segment contains wildcards the
     * whole pattern is the root and this returns its length.
     */
    static int wildcardRoot(PathMatcher pathMatcher, String pattern) {
        int start = 0;
        while (start < pattern.length()) {
            int slash = pattern.indexOf('/', start);
            String segment = slash < 0 ? pattern.substring(start) : pattern.substring(start, slash);
            if (pathMatcher.isPattern(segment)) {
                return start == 0 ? 0 : start - 1;
            }
            if (slash < 0) {
                return pattern.length();
            }
            start = slash + 1;
        }
        return start;
    }

    /**
     * Opens a connection to the URL without jar entry caching, so the caller's stream is not
     * invalidated by the shared jar cache when entries are read repeatedly.
     */
    static URLConnection openUncachedConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection jarConnection) {
            jarConnection.setUseCaches(false);
        }
        return connection;
    }
}
