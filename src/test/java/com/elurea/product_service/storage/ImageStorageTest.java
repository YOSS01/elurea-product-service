package com.elurea.product_service.storage;

import com.elurea.product_service.config.AppProperties;
import com.elurea.product_service.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageStorageTest {

    private static final byte[] PNG_HEADER = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final String BASE_URL = "http://localhost:8082/uploads";

    @TempDir
    Path tempDir;

    private ImageStorage storage;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(null, null,
                new AppProperties.Storage(tempDir.toString(), BASE_URL + "/"), null);
        storage = new ImageStorage(properties);
    }

    @Test
    void storesValidImageUnderRandomNameAndReturnsPublicUrl() throws Exception {
        // The client claims a .gif name and type: both are ignored, the content decides.
        var file = new MockMultipartFile("file", "../../evil.gif", "image/gif", PNG_HEADER);

        String url = storage.store(file, "avatars");

        assertThat(url).startsWith(BASE_URL + "/avatars/").endsWith(".png").doesNotContain("evil");
        Path stored = tempDir.resolve(url.substring(BASE_URL.length() + 1));
        assertThat(Files.readAllBytes(stored)).isEqualTo(PNG_HEADER);
    }

    @Test
    void rejectsFileThatIsNotAnImageEvenIfItClaimsToBe() {
        var file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
                "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> storage.store(file, "avatars"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("JPEG, PNG or WebP");
    }

    @Test
    void rejectsEmptyFile() {
        var file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> storage.store(file, "avatars")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteIgnoresExternalAndTraversalUrls() throws Exception {
        Path outside = Files.writeString(tempDir.getParent().resolve("keep-me-" + System.nanoTime() + ".txt"), "x");
        try {
            for (String url : List.of("https://other.cdn/a.png", BASE_URL + "/../" + outside.getFileName())) {
                storage.deleteAfterCommit(url);
            }
            assertThat(outside).exists();
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void deletesManagedFileWhenNoTransactionIsActive() {
        String url = storage.store(new MockMultipartFile("file", "a.png", "image/png", PNG_HEADER), "avatars");
        Path stored = tempDir.resolve(url.substring(BASE_URL.length() + 1));

        storage.deleteAfterCommit(url);

        assertThat(stored).doesNotExist();
    }
}
