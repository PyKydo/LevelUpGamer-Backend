package com.levelupgamer.common.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final Path basePath;
    private final String publicPrefix;
    private final String publicBaseUrl;

        public LocalFileStorageService(
            @Value("${storage.local.base-path:s3-files}") String basePath,
            @Value("${storage.local.public-url-prefix:/uploads/}") String publicPrefix,
            @Value("${app.storage.local-base-url:}") String publicBaseUrl) throws IOException {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        Files.createDirectories(this.basePath);
        this.publicPrefix = normalizePrefix(publicPrefix);
        this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
    }

    @Override
    public List<String> listPublicUrls(String folder) throws IOException {
        String sanitizedFolder = StorageKeyUtils.sanitizeFolder(folder, folder);
        Path targetFolderPath = basePath.resolve(sanitizedFolder).normalize();
        if (!targetFolderPath.startsWith(basePath) || !Files.exists(targetFolderPath)) {
            return Collections.emptyList();
        }
        try (Stream<Path> stream = Files.list(targetFolderPath)) {
            return stream.filter(Files::isRegularFile)
                    .sorted()
                    .map(path -> sanitizedFolder + "/" + path.getFileName())
                    .map(this::buildPublicUrl)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFileName, long contentLength, String folder,
            String contentType) throws IOException {
        String defaultFolder = StringUtils.hasText(folder) ? folder : LocalDate.now().toString();
        String targetFolder = StorageKeyUtils.sanitizeFolder(folder, defaultFolder);
        Path targetFolderPath = basePath.resolve(targetFolder);
        Files.createDirectories(targetFolderPath);
        String fileName = StorageKeyUtils.shortUuid() + "_" + StorageKeyUtils.sanitizeFileName(originalFileName);
        Path destination = targetFolderPath.resolve(fileName);
        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        return buildPublicUrl(targetFolder + "/" + fileName);
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

    @Override
    public boolean deleteIfManaged(String publicUrl) throws IOException {
        if (!StringUtils.hasText(publicUrl)) {
            return false;
        }

        String normalized = publicUrl.trim();
        if (normalized.startsWith("file:")) {
            Path target = Path.of(java.net.URI.create(normalized));
            return Files.deleteIfExists(target);
        }

        String relative = extractRelativePath(normalized);
        if (relative == null) {
            return false;
        }

        Path candidate = basePath.resolve(relative).normalize();
        if (!candidate.startsWith(basePath)) {
            return false;
        }
        return Files.deleteIfExists(candidate);
    }

    private String extractRelativePath(String value) {
        if (StringUtils.hasText(publicBaseUrl)) {
            String absolutePrefix = publicBaseUrl + publicPrefix;
            if (value.startsWith(absolutePrefix)) {
                return value.substring(absolutePrefix.length());
            }
        }
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

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return null;
        }
        String normalized = baseUrl.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String buildPublicUrl(String relativePath) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl + publicPrefix + relativePath;
        }
        return publicPrefix + relativePath;
    }
}
