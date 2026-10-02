package io.github.fludakit.sqlinit.resource;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Resolves locations against the classpath through a {@link ClassLoader}, scanning directories and
 * jar/zip archives.
 */
public class ClassPathResourceResolver implements ResourceResolver {

    private static final String ALL_FILES = "**/*";

    private final ClassLoader classLoader;
    private final PathMatcher pathMatcher;

    public ClassPathResourceResolver() {
        this(ResourceUtils.defaultClassLoader());
    }

    public ClassPathResourceResolver(ClassLoader classLoader) {
        this(classLoader, new AntPathMatcher());
    }

    public ClassPathResourceResolver(ClassLoader classLoader, PathMatcher pathMatcher) {
        this.classLoader = classLoader;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public Resource getResource(String location) {
        return new ClassPathResource(ResourceUtils.stripLeadingSlash(location), classLoader);
    }

    @Override
    public List<Resource> getResources(String pattern) {
        return resolveClasspath(ResourceUtils.stripLeadingSlash(pattern));
    }

    private List<Resource> resolveClasspath(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Empty classpath resource location");
        }
        if (pathMatcher.isPattern(path)) {
            return scanClasspath(path);
        }
        URL url = classLoader.getResource(path);
        if (url != null && isFile(url)) {
            return List.of(new ClassPathResource(path, classLoader));
        }
        return scanClasspath(path + "/" + ALL_FILES);
    }

    private boolean isFile(URL url) {
        try {
            if ("file".equals(url.getProtocol())) {
                return Files.isRegularFile(Paths.get(url.toURI()));
            }
            if ("jar".equals(url.getProtocol())) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                connection.setUseCaches(false);
                try (JarFile jarFile = connection.getJarFile()) {
                    JarEntry entry = jarFile.getJarEntry(connection.getEntryName());
                    return entry != null && !entry.isDirectory();
                }
            }
        } catch (IOException | URISyntaxException e) {
            return false;
        }
        return false;
    }

    private List<Resource> scanClasspath(String pattern) {
        int rootEnd = wildcardRoot(pattern);
        String root = pattern.substring(0, rootEnd);
        String entryPrefix = root.isEmpty() ? "" : root + "/";
        String relativePattern = pattern.substring(entryPrefix.length());

        List<Resource> resources = new ArrayList<>();
        try {
            Enumeration<URL> roots = classLoader.getResources(root);
            List<URL> rootList = new ArrayList<>();
            while (roots.hasMoreElements()) {
                rootList.add(roots.nextElement());
            }
            for (URL rootUrl : rootList) {
                scanRoot(rootUrl, entryPrefix, relativePattern, resources);
            }
        } catch (IOException | URISyntaxException e) {
            throw new ResourceException("Failed to scan the classpath for resources at: " + pattern, e);
        }
        resources.sort(Comparator.comparing(Resource::getFilename));
        return resources;
    }

    private void scanRoot(URL rootUrl, String entryPrefix, String relativePattern, List<Resource> resources)
            throws IOException, URISyntaxException {
        switch (rootUrl.getProtocol()) {
            case "file" -> scanDirectory(Paths.get(rootUrl.toURI()), relativePattern, resources);
            case "jar" -> scanJar(rootUrl, entryPrefix, relativePattern, resources);
            case "vfs" -> scanVfsDirectory(rootUrl, relativePattern, resources);
            default -> { /* unsupported classpath root, skip */ }
        }
    }

    /**
     * Scans a WildFly VFS (Virtual File System) directory for resources.
     *
     * <p>WildFly uses a virtual file system for deployed applications. When resources are packaged
     * in a WAR/EAR and deployed to WildFly, the classloader returns URLs with the {@code vfs:} protocol
     * instead of {@code file:} or {@code jar:}. For example:</p>
     *
     * <pre>
     * vfs:/D:/wildfly/standalone/deployments/app.war/WEB-INF/classes/db/migration/
     * </pre>
     *
     * <p>These VFS URLs cannot be converted to {@link java.nio.file.Path} objects directly. Instead,
     * we use WildFly's VFS API ({@code org.jboss.vfs.VirtualFile}) to traverse the virtual directory
     * structure and read file contents. The VFS dependency is optional and only used when running on
     * WildFly; on other application servers, this method is a no-op.</p>
     */
    private void scanVfsDirectory(URL rootUrl, String relativePattern, List<Resource> resources) throws IOException {
        if (!isVfsPresent()) {
            return;
        }
        try {
            org.jboss.vfs.VirtualFile virtualFile = (org.jboss.vfs.VirtualFile) rootUrl.openConnection().getContent();
            scanVfsVirtualFile(virtualFile, relativePattern, resources);
        } catch (Exception e) {
            throw new ResourceException("Failed to scan VFS directory: " + rootUrl, e);
        }
    }

    private static boolean isVfsPresent() {
        try {
            Class.forName("org.jboss.vfs.VirtualFile");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void scanVfsVirtualFile(org.jboss.vfs.VirtualFile virtualFile, String relativePattern, List<Resource> resources) {
        for (org.jboss.vfs.VirtualFile child : virtualFile.getChildren()) {
            if (child.isFile() && pathMatcher.match(relativePattern, child.getName())) {
                try {
                    resources.add(new UrlResource(child.toURL()));
                } catch (Exception e) {
                    throw new ResourceException("Failed to convert VFS file to URL: " + child, e);
                }
            } else if (!child.isFile()) {
                scanVfsVirtualFile(child, relativePattern, resources);
            }
        }
    }

    private void scanDirectory(Path root, String relativePattern, List<Resource> resources) throws IOException {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                String relative = root.relativize(file).toString().replace('\\', '/');
                if (pathMatcher.match(relativePattern, relative)) {
                    resources.add(new UrlResource(toUrl(file)));
                }
            });
        }
    }

    private void scanJar(URL rootUrl, String entryPrefix, String relativePattern, List<Resource> resources)
            throws IOException, URISyntaxException {
        JarURLConnection connection = (JarURLConnection) rootUrl.openConnection();
        connection.setUseCaches(false);
        URI jarUri = connection.getJarFileURL().toURI();
        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.startsWith(entryPrefix)) {
                    continue;
                }
                String relative = name.substring(entryPrefix.length());
                if (pathMatcher.match(relativePattern, relative)) {
                    resources.add(new UrlResource(
                            new URI("jar", jarUri.toASCIIString() + "!/" + name, null).toURL()));
                }
            }
        }
    }

    private static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new ResourceException("Failed to convert path to URL: " + path, e);
        }
    }

    private int wildcardRoot(String pattern) {
        int start = 0;
        while (start < pattern.length()) {
            int slash = pattern.indexOf('/', start);
            String segment = slash < 0 ? pattern.substring(start) : pattern.substring(start, slash);
            if (pathMatcher.isPattern(segment)) {
                return start == 0 ? 0 : start - 1;
            }
            if (slash < 0) {
                return pattern.length();
            }
            start = slash + 1;
        }
        return start;
    }
}
