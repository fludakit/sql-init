package io.github.fludakit.sqlinit.resource;

import java.util.List;

/**
 * Resolves a location for a single protocol into one or more {@link Resource}s.
 *
 * <p>A resolver handles a location without its protocol prefix: {@code classpath:},
 * {@code file:}/{@code filesystem:}, or a custom scheme such as {@code s3:}.</p>
 *
 * <p>Implementations must not declare checked exceptions: translate I/O and provider failures to
 * {@link ResourceException} (unchecked) with the offending location in the message and the
 * original exception as the cause. See the built-in resolvers for the pattern.</p>
 */
public interface ResourceResolver {

    /**
     * The protocol this resolver handles (without the trailing colon), or {@code null} when it is
     * registered by other means (as the built-in {@code classpath:}/{@code file:} resolvers are).
     */
    default String protocol() {
        return null;
    }

    /**
     * Resolves a literal location to a single resource, which may not exist.
     *
     * @throws ResourceException if resolution fails for reasons other than the resource not existing
     */
    Resource getResource(String location);

    /**
     * Resolves a location pattern (literal file, directory, or Ant-style pattern) into the resources
     * it matches, sorted by filename.
     *
     * @throws ResourceException if the location cannot be scanned
     */
    List<Resource> getResources(String locationPattern);
}
