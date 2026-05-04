package com.unihub.shared.storage;

import com.unihub.shared.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    @Value("${storage.local.base-path:uploads}")
    private String basePath;

    @Value("${storage.local.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg",      ".jpg",
            "image/png",       ".png",
            "image/webp",      ".webp",
            "application/pdf", ".pdf"
    );

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    @Override
    public String upload(MultipartFile file, String path) {

        // Null / empty check
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        // Size check
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds the 10 MB limit");
        }

        // MIME type check
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.containsKey(contentType)) {
            throw new BadRequestException(
                    "File type not allowed. Supported types: JPEG, PNG, WEBP, PDF");
        }

        String extension = ALLOWED_TYPES.get(contentType);
        String safeFilename = UUID.randomUUID() + extension;

        try {
            Path targetDir = Paths.get(basePath, path).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(safeFilename).normalize();

            // Extra guard: ensure resolved path is still inside targetDir
            if (!targetPath.startsWith(targetDir)) {
                throw new BadRequestException("Invalid file path");
            }

            Files.copy(file.getInputStream(), targetPath);
            return baseUrl + "/" + path + "/" + safeFilename;

        } catch (IOException e) {
            log.error("Failed to store file in path={}: {}", path, e.getMessage());
            throw new RuntimeException("Failed to store file");
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Path filePath = Paths.get(basePath, relativePath).toAbsolutePath().normalize();

            // Guard: never delete outside basePath
            Path base = Paths.get(basePath).toAbsolutePath().normalize();
            if (!filePath.startsWith(base)) {
                log.warn("Attempted to delete file outside basePath: {}", fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
        }
    }
}