package io.github.fludakit.sqlinit.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Resource} backed by a file on the file system.
 */
public class FileSystemResource implements Resource {

    private final Path path;

    public FileSystemResource(Path path) {
        this.path = path;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new ResourceException("Failed to open file: " + path, e);
        }
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public long contentLength() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new ResourceException("Failed to read file size: " + path, e);
        }
    }

    @Override
    public URL getURL() {
        try {
            return path.toUri().toURL();
        } catch (IOException e) {
            throw new ResourceException("Failed to convert path to URL: " + path, e);
        }
    }

    @Override
    public String getFilename() {
        Path fileName = path.getFileName();
        return fileName == null ? null : fileName.toString();
    }
}
