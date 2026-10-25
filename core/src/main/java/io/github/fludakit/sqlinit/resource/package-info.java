/**
 * Uniform resource access for migration scripts: classpath entries, files, URLs, and custom
 * providers.
 *
 * <h2>Exception contract</h2>
 * <p>This package's API is entirely unchecked: {@link io.github.fludakit.sqlinit.resource.Resource}
 * and {@link io.github.fludakit.sqlinit.resource.ResourceResolver} declare no checked exceptions.
 * Implementations translate their I/O and provider failures to
 * {@link io.github.fludakit.sqlinit.resource.ResourceException} — with the offending location in
 * the message and the original exception as the cause — exactly as the built-in
 * {@code ClassPathResourceResolver}, {@code FileSystemResourceResolver}, and resource classes do.
 * Custom resolvers should follow the same rule so callers get one predictable exception family.</p>
 *
 * <p>{@link io.github.fludakit.sqlinit.DbMigrator} wraps any escaping {@code ResourceException}
 * into its own {@code SqlInitException}, so end users of the migration API see a single unchecked
 * hierarchy with the original cause preserved.</p>
 */
package io.github.fludakit.sqlinit.resource;
