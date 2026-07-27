package com.flowforge.sources.storage;

import com.flowforge.config.FlowForgeProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.stereotype.Component;

@Component
public class LocalFileSystemSourceStorage implements SourceStorage {
    private final Path root;

    public LocalFileSystemSourceStorage(FlowForgeProperties properties) {
        this.root = properties.sources().storageRoot().toAbsolutePath().normalize();
    }

    @Override
    public String store(
            String organizationId,
            String workspaceId,
            String sourceId,
            String originalFilename,
            byte[] content
    ) {
        String safeFilename = safeFilename(originalFilename);
        Path target = root
                .resolve(safeSegment(organizationId))
                .resolve(safeSegment(workspaceId))
                .resolve(safeSegment(sourceId))
                .resolve(safeFilename)
                .normalize();
        if (!target.startsWith(root)) {
            throw new IllegalStateException("Resolved source storage path escapes configured root");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return root.relativize(target).toString().replace('\\', '/');
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store source document", exception);
        }
    }

    @Override
    public void delete(String storagePath) {
        Path target = root.resolve(storagePath).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid source storage path");
        }
        try {
            Files.deleteIfExists(target);
            deleteEmptyParents(target.getParent());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete source document", exception);
        }
    }

    private void deleteEmptyParents(Path directory) throws IOException {
        Path current = directory;
        while (current != null && !current.equals(root) && Files.isDirectory(current)) {
            try (var entries = Files.list(current)) {
                if (entries.findAny().isPresent()) {
                    return;
                }
            }
            Files.deleteIfExists(current);
            current = current.getParent();
        }
    }

    private String safeFilename(String filename) {
        String value = filename == null ? "source" : filename.replace('\\', '/');
        int separator = value.lastIndexOf('/');
        String leaf = separator >= 0 ? value.substring(separator + 1) : value;
        String sanitized = leaf.replaceAll("[^A-Za-z0-9._-]", "_");
        sanitized = sanitized.replaceFirst("\\.+$", "");
        if (sanitized.isBlank() || ".".equals(sanitized) || "..".equals(sanitized)) {
            return "source";
        }
        String basename = sanitized.contains(".")
                ? sanitized.substring(0, sanitized.indexOf('.'))
                : sanitized;
        if (basename.matches("(?i)CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9]")) {
            return "_" + sanitized;
        }
        return sanitized;
    }

    private String safeSegment(String segment) {
        String sanitized = segment.replaceAll("[^A-Za-z0-9_-]", "_");
        if (sanitized.isBlank()) {
            throw new IllegalArgumentException("Storage path segment is blank");
        }
        return sanitized;
    }
}
