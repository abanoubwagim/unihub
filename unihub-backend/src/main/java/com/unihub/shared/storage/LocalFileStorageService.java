package com.unihub.shared.storage;

import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.InvalidOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Profile("!prod")
public class LocalFileStorageService implements FileStorageService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "application/pdf", ".pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx");

    private static final Set<String> ZIP_BASED_OFFICE_MIMES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation");

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB
    @Value("${storage.local.base-path:uploads}")
    private String basePath;
    @Value("${storage.local.base-url:http://localhost:8080}")
    private String baseUrl;

    private static String detectMimeFromBytes(byte[] b) {
        if (b.length < 4) {
            return "application/octet-stream";
        }

        // JPEG: FF D8 FF
        if ((b[0] & 0xFF) == 0xFF
                && (b[1] & 0xFF) == 0xD8
                && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((b[0] & 0xFF) == 0x89
                && (b[1] & 0xFF) == 0x50
                && (b[2] & 0xFF) == 0x4E
                && (b[3] & 0xFF) == 0x47) {
            return "image/png";
        }

        // WebP: RIFF (offset 0-3) + WEBP (offset 8-11)
        if (b.length >= 12
                && (b[0] & 0xFF) == 0x52 // R
                && (b[1] & 0xFF) == 0x49 // I
                && (b[2] & 0xFF) == 0x46 // F
                && (b[3] & 0xFF) == 0x46 // F
                && (b[8] & 0xFF) == 0x57 // W
                && (b[9] & 0xFF) == 0x45 // E
                && (b[10] & 0xFF) == 0x42 // B
                && (b[11] & 0xFF) == 0x50) { // P
            return "image/webp";
        }

        // PDF: %PDF (25 50 44 46)
        if ((b[0] & 0xFF) == 0x25
                && (b[1] & 0xFF) == 0x50
                && (b[2] & 0xFF) == 0x44
                && (b[3] & 0xFF) == 0x46) {
            return "application/pdf";
        }

        // ZIP / Office Open XML (docx, pptx, xlsx): PK (50 4B 03 04)
        if ((b[0] & 0xFF) == 0x50 && (b[1] & 0xFF) == 0x4B
                && (b[2] & 0xFF) == 0x03 && (b[3] & 0xFF) == 0x04)
            return "application/zip";

        return "application/octet-stream";
    }

    @Override
    public String upload(MultipartFile file, String path) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("File size exceeds the 10 MB limit");
        }

        byte[] headerBytes = readHeaderBytes(file);
        String detectedMime = detectMimeFromBytes(headerBytes);
        String declaredMime = file.getContentType();
        String effectiveMime = validateAndResolveType(detectedMime, declaredMime);

        String extension = ALLOWED_TYPES.get(effectiveMime);
        String safeFilename = UUID.randomUUID() + extension;

        try {
            Path targetDir = Paths.get(basePath, path).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(safeFilename).normalize();

            if (!targetPath.startsWith(targetDir)) {
                throw new BadRequestException("Invalid file path");
            }

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.debug("File stored: path={}/{}", path, safeFilename);
            return baseUrl + "/" + path + "/" + safeFilename;

        } catch (IOException e) {
            log.error("Failed to store file in path={}: {}", path, e.getMessage(), e);
            throw new InvalidOperationException("Failed to store file. Please try again.");
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank())
            return;

        try {
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            Path filePath = Paths.get(basePath, relativePath).toAbsolutePath().normalize();

            // Guard: never delete anything outside basePath
            Path base = Paths.get(basePath).toAbsolutePath().normalize();
            if (!filePath.startsWith(base)) {
                log.warn("Attempted to delete file outside basePath: {}", fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);
            log.debug("File deleted: {}", fileUrl);

        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", fileUrl, e.getMessage());
        }
    }

    private String validateAndResolveType(String detectedMime, String declaredMime) {

        if ("application/zip".equals(detectedMime)) {
            if (declaredMime != null && ZIP_BASED_OFFICE_MIMES.contains(declaredMime)) {
                return declaredMime; // trust declared for office formats
            }
            throw new BadRequestException(
                    "File type not allowed. Supported types: JPEG, PNG, WEBP, PDF, DOCX, PPTX");
        }

        if (declaredMime != null && !declaredMime.equals(detectedMime)) {
            log.warn("Content-Type mismatch — declared={}, detected={}", declaredMime, detectedMime);
            throw new BadRequestException(
                    "File content does not match the declared content type");
        }

        if (!ALLOWED_TYPES.containsKey(detectedMime)) {
            throw new BadRequestException(
                    "File type not allowed. Supported types: JPEG, PNG, WEBP, PDF, DOCX, PPTX");
        }

        return detectedMime;
    }

    private byte[] readHeaderBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[32];
            int bytesRead = is.read(header, 0, 12);

            if (bytesRead < 4) {
                throw new BadRequestException("File is too small to determine its type");
            }

            return header;

        } catch (IOException e) {
            log.error("Failed to read file header bytes: {}", e.getMessage());
            throw new BadRequestException("Unable to read the uploaded file");
        }
    }
}