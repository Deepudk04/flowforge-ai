package com.flowforge.sources.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowforge.config.FlowForgeProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileSystemSourceStorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void storesWithinTenantWorkspaceAndDeletesTheStoredFile() throws Exception {
        var storage = new LocalFileSystemSourceStorage(properties(temporaryDirectory));

        String storedPath = storage.store(
                "org_a",
                "ws_123",
                "src_123",
                "../unsafe guide?.md",
                new byte[]{1, 2, 3}
        );

        Path storedFile = temporaryDirectory.resolve(storedPath);
        assertThat(storedPath).isEqualTo("org_a/ws_123/src_123/unsafe_guide_.md");
        assertThat(storedFile).hasBinaryContent(new byte[]{1, 2, 3});

        storage.delete(storedPath);

        assertThat(Files.exists(storedFile)).isFalse();
        assertThat(Files.exists(temporaryDirectory.resolve("org_a"))).isFalse();
    }

    private static FlowForgeProperties properties(Path storageRoot) {
        return new FlowForgeProperties(
                "/api/v1",
                "http://localhost:5173",
                new FlowForgeProperties.Security(false, "flowforge-api"),
                new FlowForgeProperties.AiService("local", "http://localhost:8000", 1000, 1, 0),
                new FlowForgeProperties.Sources(storageRoot, 1024, 10)
        );
    }
}
