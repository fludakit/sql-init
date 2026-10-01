package io.github.fludakit.sqlinit.resource;

/**
 * Thrown when a {@link Resource} cannot be located, opened, or read.
 *
 * <p>Wraps the underlying {@link java.io.IOException} so that callers of the resource API do not
 * have to handle checked exceptions.</p>
 */
public class ResourceException extends RuntimeException {

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
