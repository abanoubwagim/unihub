package com.unihub.shared.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.unihub.shared.exception.BadRequestException;
import com.unihub.shared.exception.InvalidOperationException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Profile("prod")
public class S3FileStorageService implements FileStorageService {

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

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    @Value("${storage.s3.bucket}")
    private String bucket;

    @Value("${storage.s3.base-url}")
    private String baseUrl;

    private final S3Client s3Client;

    public S3FileStorageService(
            @Value("${storage.s3.region}") String region) {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
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
        String safeKey = path + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(safeKey)
                    .contentType(effectiveMime)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            String url = baseUrl + "/" + safeKey;
            log.debug("File uploaded to S3 — key={}", safeKey);
            return url;

        } catch (IOException e) {
            log.error("Failed to upload file to S3 — path={}, error={}", path, e.getMessage(), e);
            throw new InvalidOperationException("Failed to store file. Please try again.");
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank())
            return;

        try {
            String key = fileUrl.replace(baseUrl + "/", "");
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.debug("File deleted from S3 — key={}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3 — url={}, error={}", fileUrl, e.getMessage());
        }
    }

    private String validateAndResolveType(String detectedMime, String declaredMime) {
        if ("application/zip".equals(detectedMime)) {
            if (declaredMime != null && ZIP_BASED_OFFICE_MIMES.contains(declaredMime)) {
                return declaredMime;
            }
            throw new BadRequestException(
                    "File type not allowed. Supported types: JPEG, PNG, WEBP, PDF, DOCX, PPTX");
        }
        if (declaredMime != null && !declaredMime.equals(detectedMime)) {
            log.warn("Content-Type mismatch — declared={}, detected={}", declaredMime, detectedMime);
            throw new BadRequestException("File content does not match the declared content type");
        }
        if (!ALLOWED_TYPES.containsKey(detectedMime)) {
            throw new BadRequestException(
                    "File type not allowed. Supported types: JPEG, PNG, WEBP, PDF, DOCX, PPTX");
        }
        return detectedMime;
    }

    private byte[] readHeaderBytes(MultipartFile file) {
        try (var is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header, 0, 12);
            if (read < 4)
                throw new BadRequestException("File is too small to determine its type");
            return header;
        } catch (IOException e) {
            throw new BadRequestException("Unable to read the uploaded file");
        }
    }

    private static String detectMimeFromBytes(byte[] b) {
        if (b.length < 4)
            return "application/octet-stream";
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF)
            return "image/jpeg";
        if ((b[0] & 0xFF) == 0x89 && (b[1] & 0xFF) == 0x50
                && (b[2] & 0xFF) == 0x4E && (b[3] & 0xFF) == 0x47)
            return "image/png";
        if (b.length >= 12
                && (b[0] & 0xFF) == 0x52 && (b[1] & 0xFF) == 0x49
                && (b[2] & 0xFF) == 0x46 && (b[3] & 0xFF) == 0x46
                && (b[8] & 0xFF) == 0x57 && (b[9] & 0xFF) == 0x45
                && (b[10] & 0xFF) == 0x42 && (b[11] & 0xFF) == 0x50)
            return "image/webp";
        if ((b[0] & 0xFF) == 0x25 && (b[1] & 0xFF) == 0x50
                && (b[2] & 0xFF) == 0x44 && (b[3] & 0xFF) == 0x46)
            return "application/pdf";
        if ((b[0] & 0xFF) == 0x50 && (b[1] & 0xFF) == 0x4B
                && (b[2] & 0xFF) == 0x03 && (b[3] & 0xFF) == 0x04)
            return "application/zip";
        return "application/octet-stream";
    }
}