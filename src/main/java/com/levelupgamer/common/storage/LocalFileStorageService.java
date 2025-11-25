package com.levelupgamer.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path basePath;
    private final String publicPrefix;

    public LocalFileStorageService(
            @Value("${storage.local.base-path:s3-files/uploads}") String basePath,
            @Value("${storage.local.public-url-prefix:/uploads/}") String publicPrefix) throws IOException {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        Files.createDirectories(this.basePath);
        this.publicPrefix = normalizePrefix(publicPrefix);
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFileName, long contentLength) throws IOException {
        String extension = extractExtension(originalFileName);
        String folder = LocalDate.now().toString();
        Path targetFolder = basePath.resolve(folder);
        Files.createDirectories(targetFolder);
        String fileName = UUID.randomUUID() + extension;
        Path destination = targetFolder.resolve(fileName);
        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        return publicPrefix + folder + "/" + fileName;
    }

    @Override
    public Optional<String> readContentIfManaged(String publicUrl) throws IOException {
        if (!StringUtils.hasText(publicUrl)) {
            return Optional.empty();
        }

        String normalized = publicUrl.trim();
        if (normalized.startsWith("file:")) {
            return Optional.of(Files.readString(Path.of(java.net.URI.create(normalized))));
        }

        String relative = extractRelativePath(normalized);
        if (relative == null) {
            return Optional.empty();
        }

        Path candidate = basePath.resolve(relative).normalize();
        if (!candidate.startsWith(basePath) || !Files.exists(candidate)) {
            return Optional.empty();
        }
        return Optional.of(Files.readString(candidate));
    }

    private String extractRelativePath(String value) {
        if (value.startsWith(publicPrefix)) {
            return value.substring(publicPrefix.length());
        }
        try {
            java.net.URI uri = java.net.URI.create(value);
            String path = uri.getPath();
            if (StringUtils.hasText(path) && path.startsWith(publicPrefix)) {
                return path.substring(publicPrefix.length());
            }
        } catch (IllegalArgumentException ignored) {
            
        }
        if (value.startsWith("local://")) {
            return value.substring("local://".length());
        }
        return null;
    }

    private String extractExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "";
        }
        String filename = Paths.get(originalFileName).getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex);
        }
        return "";
    }

    private String normalizePrefix(String prefix) {
        String normalized = prefix;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }
}
