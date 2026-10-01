package io.github.fludakit.sqlinit.resource;

import java.io.InputStream;
import java.net.URL;

/**
 * A physical resource: a file, a classpath entry, or a URL, accessed uniformly.
 */
public interface Resource {

    InputStream getInputStream();

    boolean exists();

    long contentLength();

    URL getURL();

    /** The filename (last path segment) of this resource, or {@code null} if it has none. */
    String getFilename();
}
