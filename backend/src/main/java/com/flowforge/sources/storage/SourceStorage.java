package com.flowforge.sources.storage;

public interface SourceStorage {
    String store(
            String organizationId,
            String workspaceId,
            String sourceId,
            String originalFilename,
            byte[] content
    );

    void delete(String storagePath);
}
