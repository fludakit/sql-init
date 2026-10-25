package io.github.fludakit.sqlinit.resource;

import java.io.InputStream;
import java.net.URL;

/**
 * A physical resource: a file, a classpath entry, or a URL, accessed uniformly.
 *
 * <p>Implementations must not declare checked exceptions: I/O failures are translated to
 * {@link ResourceException} (unchecked) with the offending location in the message and the
 * original exception as the cause, so callers never handle {@code IOException} directly.</p>
 */
public interface Resource {

    /**
     * Opens the resource for reading.
     *
     * @throws ResourceException if the resource cannot be opened or read
     */
    InputStream getInputStream();

    boolean exists();

    /**
     * The resource's byte length.
     *
     * @throws ResourceException if the length cannot be determined
     */
    long contentLength();

    URL getURL();

    /** The filename (last path segment) of this resource, or {@code null} if it has none. */
    String getFilename();
}
