package io.github.fludakit.sqlinit;

/**
 * Thrown when a SQL migration cannot be resolved, parsed, or applied.
 *
 * <p>This is the primary unchecked exception for the public {@link DbMigrator} API. Internal causes
 * such as I/O failures, parse errors, or JDBC errors are wrapped as the {@linkplain #getCause()
 * cause}.</p>
 */
public class SqlInitException extends RuntimeException {

    public SqlInitException(String message) {
        super(message);
    }

    public SqlInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
