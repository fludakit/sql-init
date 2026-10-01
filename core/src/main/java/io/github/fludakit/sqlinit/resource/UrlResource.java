package io.github.fludakit.sqlinit.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link Resource} backed by a resolved {@link URL}, such as a classpath or jar entry.
 */
public class UrlResource implements Resource {

    private final URL url;

    public UrlResource(URL url) {
        this.url = url;
    }

    @Override
    public InputStream getInputStream() {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection jarConnection) {
                jarConnection.setUseCaches(false);
            }
            return connection.getInputStream();
        } catch (IOException e) {
            throw new ResourceException("Failed to open URL resource: " + url, e);
        }
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public long contentLength() {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection jarConnection) {
                jarConnection.setUseCaches(false);
            }
            return connection.getContentLengthLong();
        } catch (IOException e) {
            throw new ResourceException("Failed to read content length for URL: " + url, e);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }

    @Override
    public String getFilename() {
        return filename(url.getPath());
    }

    private static String filename(String path) {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash < 0 ? normalized : normalized.substring(slash + 1);
    }
}
