package io.github.fludakit.sqlinit.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link Resource} resolved from the classpath through a {@link ClassLoader}.
 */
public class ClassPathResource implements Resource {

    private final String path;
    private final URL url;

    public ClassPathResource(String path, ClassLoader classLoader) {
        this.path = path;
        this.url = classLoader.getResource(path);
    }

    @Override
    public InputStream getInputStream() {
        try {
            URLConnection connection = url().openConnection();
            if (connection instanceof JarURLConnection jarConnection) {
                jarConnection.setUseCaches(false);
            }
            return connection.getInputStream();
        } catch (IOException e) {
            throw new ResourceException("Failed to open classpath resource: " + path, e);
        }
    }

    @Override
    public boolean exists() {
        return url != null;
    }

    @Override
    public long contentLength() {
        try {
            URLConnection connection = url().openConnection();
            if (connection instanceof JarURLConnection jarConnection) {
                jarConnection.setUseCaches(false);
            }
            return connection.getContentLengthLong();
        } catch (IOException e) {
            throw new ResourceException("Failed to read content length for: " + path, e);
        }
    }

    @Override
    public URL getURL() {
        return url();
    }

    @Override
    public String getFilename() {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash < 0 ? normalized : normalized.substring(slash + 1);
    }

    private URL url() {
        if (url == null) {
            throw new ResourceException("Classpath resource not found: " + path);
        }
        return url;
    }
}
