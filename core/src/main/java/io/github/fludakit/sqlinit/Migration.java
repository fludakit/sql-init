package io.github.fludakit.sqlinit;

import io.github.fludakit.sqlinit.resource.Resource;

/**
 * A single versioned migration parsed from a resolved script resource.
 */
record Migration(int version, String description, Resource resource) {

    String script() {
        return resource.getFilename();
    }
}
