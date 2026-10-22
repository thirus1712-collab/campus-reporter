package com.campusreporter.service;

import com.campusreporter.exception.ValidationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {

    private static final Path EVIDENCE_STORAGE_DIR = Paths.get("uploads", "evidence").toAbsolutePath().normalize();
    private static final Path COMPLAINT_STORAGE_DIR = Paths.get("uploads", "complaints").toAbsolutePath().normalize();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    public FileStorageService() {
        try {
            Files.createDirectories(EVIDENCE_STORAGE_DIR);
            Files.createDirectories(COMPLAINT_STORAGE_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage folder", e);
        }
    }

    /**
     * Validate and store 0 to 5 student complaint images on disk.
     * Returns a list of relative API URLs: /api/complaints/{ticketId}/images/{filename}
     */
    public List<String> storeComplaintImages(String ticketId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<MultipartFile> validFiles = files.stream()
            .filter(f -> f != null && !f.isEmpty() && f.getSize() > 0)
            .toList();

        if (validFiles.isEmpty()) {
            return Collections.emptyList();
        }

        if (validFiles.size() > 5) {
            throw new ValidationException("Maximum of 5 complaint images are permitted. You provided " + validFiles.size() + ".");
        }

        List<String> generatedUrls = new ArrayList<>();
        String safeTicketId = (ticketId != null ? ticketId.replaceAll("[^a-zA-Z0-9_-]", "") : "ticket").toUpperCase();

        for (int i = 0; i < validFiles.size(); i++) {
            MultipartFile file = validFiles.get(i);

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new ValidationException("File '" + file.getOriginalFilename() + "' exceeds the maximum permitted size of 10MB.");
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new ValidationException("Invalid image format for file '" + originalFilename + "'. Only JPG, PNG, and WebP are allowed.");
            }

            String contentType = file.getContentType();
            if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                throw new ValidationException("Invalid MIME type for file '" + originalFilename + "': " + contentType);
            }

            // Secure unique filename: complaint-<ticketId>-<timestamp>-<uuid>-<index>.<ext>
            String uniqueFilename = String.format("complaint-%s-%d-%s-%d.%s",
                safeTicketId.toLowerCase(),
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8),
                (i + 1),
                ext
            );

            Path destinationPath = COMPLAINT_STORAGE_DIR.resolve(uniqueFilename).normalize();

            // Guard against path traversal
            if (!destinationPath.startsWith(COMPLAINT_STORAGE_DIR)) {
                throw new ValidationException("Invalid storage destination for file: " + originalFilename);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to securely write complaint image to disk: " + uniqueFilename, e);
            }

            generatedUrls.add("/api/complaints/" + safeTicketId + "/images/" + uniqueFilename);
        }

        return generatedUrls;
    }

    /**
     * Load a complaint image file as a Spring Resource with path traversal protection.
     */
    public Resource loadComplaintFile(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return null;
            }

            Path filePath = COMPLAINT_STORAGE_DIR.resolve(filename).normalize();
            if (!filePath.startsWith(COMPLAINT_STORAGE_DIR) || !Files.exists(filePath) || !Files.isReadable(filePath)) {
                // Fallback to evidence dir if legacy file
                Path fallback = EVIDENCE_STORAGE_DIR.resolve(filename).normalize();
                if (fallback.startsWith(EVIDENCE_STORAGE_DIR) && Files.exists(fallback) && Files.isReadable(fallback)) {
                    Resource fallbackRes = new UrlResource(fallback.toUri());
                    if (fallbackRes.exists() && fallbackRes.isReadable()) {
                        return fallbackRes;
                    }
                }
                return null;
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Validate and store 1 to 5 repair evidence images on disk.
     * Returns a list of public relative API URLs: /api/files/evidence/{filename}
     */
    public List<String> storeEvidenceImages(String ticketId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("At least one repair evidence image is required (minimum 1, maximum 5).");
        }

        // Filter out empty entries if any
        List<MultipartFile> validFiles = files.stream()
            .filter(f -> f != null && !f.isEmpty() && f.getSize() > 0)
            .toList();

        if (validFiles.isEmpty()) {
            throw new ValidationException("At least one repair evidence image is required (minimum 1, maximum 5).");
        }

        if (validFiles.size() > 5) {
            throw new ValidationException("Maximum of 5 repair evidence images are permitted. You provided " + validFiles.size() + ".");
        }

        List<String> generatedUrls = new ArrayList<>();
        String safeTicketId = (ticketId != null ? ticketId.replaceAll("[^a-zA-Z0-9_-]", "") : "ticket").toLowerCase();

        for (int i = 0; i < validFiles.size(); i++) {
            MultipartFile file = validFiles.get(i);

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new ValidationException("File '" + file.getOriginalFilename() + "' exceeds the maximum permitted size of 10MB.");
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new ValidationException("Invalid image format for file '" + originalFilename + "'. Only JPG, PNG, and WebP are allowed.");
            }

            String contentType = file.getContentType();
            if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
                throw new ValidationException("Invalid MIME type for file '" + originalFilename + "': " + contentType);
            }

            // Secure unique filename: evidence-<ticketId>-<timestamp>-<uuid>.<ext>
            String uniqueFilename = String.format("evidence-%s-%d-%s-%d.%s",
                safeTicketId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8),
                (i + 1),
                ext
            );

            Path destinationPath = EVIDENCE_STORAGE_DIR.resolve(uniqueFilename).normalize();

            // Guard against path traversal
            if (!destinationPath.startsWith(EVIDENCE_STORAGE_DIR)) {
                throw new ValidationException("Invalid storage destination for file: " + originalFilename);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to securely write evidence image to disk: " + uniqueFilename, e);
            }

            generatedUrls.add("/api/files/evidence/" + uniqueFilename);
        }

        return generatedUrls;
    }

    /**
     * Load an evidence image file as a Spring Resource with path traversal protection.
     */
    public Resource loadEvidenceFile(String filename) {
        try {
            if (filename == null || filename.trim().isEmpty() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return null;
            }

            Path filePath = EVIDENCE_STORAGE_DIR.resolve(filename).normalize();
            if (!filePath.startsWith(EVIDENCE_STORAGE_DIR) || !Files.exists(filePath) || !Files.isReadable(filePath)) {
                return null;
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            return null;
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
