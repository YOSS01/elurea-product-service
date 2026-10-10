package com.elurea.product_service.storage;

import com.elurea.product_service.config.AppProperties;
import com.elurea.product_service.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores uploaded images on the local disk and serves them under /uploads (see WebConfig).
 * Files get random names, and the real type is detected from the file content, never from the
 * client-supplied name or Content-Type. Swap this class for an S3/Cloudinary implementation in production.
 */
@Slf4j
@Component
public class ImageStorage {

    private final Path root;
    private final String publicBaseUrl;

    public ImageStorage(AppProperties properties) {
        this.root = Paths.get(properties.storage().localDir()).toAbsolutePath().normalize();
        this.publicBaseUrl = stripTrailingSlash(properties.storage().publicBaseUrl());
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create upload directory " + root, e);
        }
    }

    public Path root() {
        return root;
    }

    /**
     * Validates and saves the image, returning its public URL. If the surrounding transaction rolls back,
     * the file is removed again so no orphan is left on disk.
     */
    public String store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("The file is empty");
        }
        ImageType type = detectType(file);

        String relative = folder + "/" + UUID.randomUUID() + "." + type.extension;
        Path target = resolveInsideRoot(relative);
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store the uploaded file", e);
        }

        runOnRollback(() -> deleteQuietly(target));
        return publicBaseUrl + "/" + relative;
    }

    /**
     * Deletes a file previously returned by {@link #store}, once the current transaction has committed.
     * URLs pointing elsewhere (external images) are ignored.
     */
    public void deleteAfterCommit(String url) {
        toManagedPath(url).ifPresent(path -> runAfterCommit(() -> deleteQuietly(path)));
    }

    private Optional<Path> toManagedPath(String url) {
        if (url == null || !url.startsWith(publicBaseUrl + "/")) {
            return Optional.empty();
        }
        try {
            return Optional.of(resolveInsideRoot(url.substring(publicBaseUrl.length() + 1)));
        } catch (BadRequestException e) {
            return Optional.empty();
        }
    }

    /** Guards against path traversal ("../") in any path built from a URL. */
    private Path resolveInsideRoot(String relative) {
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root)) {
            throw new BadRequestException("Invalid file path");
        }
        return resolved;
    }

    private static ImageType detectType(MultipartFile file) {
        byte[] header = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the uploaded file", e);
        }
        return Arrays.stream(ImageType.values())
                .filter(type -> type.matches(header, read))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Only JPEG, PNG or WebP images are allowed"));
    }

    private static void runOnRollback(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        action.run();
                    }
                }
            });
        }
    }

    private static void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete file {}", path, e);
        }
    }

    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("app.storage.public-base-url must be configured");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Accepted formats, recognised by their magic bytes. SVG is deliberately excluded (it can carry scripts). */
    private enum ImageType {
        JPEG("jpg") {
            @Override
            boolean matches(byte[] h, int n) {
                return n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
            }
        },
        PNG("png") {
            @Override
            boolean matches(byte[] h, int n) {
                return n >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                        && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A;
            }
        },
        WEBP("webp") {
            @Override
            boolean matches(byte[] h, int n) {
                return n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                        && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            }
        };

        private final String extension;

        ImageType(String extension) {
            this.extension = extension;
        }

        abstract boolean matches(byte[] header, int length);
    }
}
