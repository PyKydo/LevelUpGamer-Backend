package com.levelupgamer.contenido;

import com.levelupgamer.config.AwsStorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class BlogAssetStorageService {

    private static final String BLOG_FOLDER = "blogs";
    private final AwsStorageProperties awsStorageProperties;
    private final Path localBasePath;
    private final String localUploadsPrefix;
    private final String localBaseUrl;
    private final S3Client s3Client;

    public BlogAssetStorageService(
            AwsStorageProperties awsStorageProperties,
            @Value("${storage.local.base-path:${user.dir}/s3-files}") String localBasePath,
            @Value("${storage.local.public-url-prefix:/uploads/}") String localPublicPrefix,
            @Value("${app.storage.local-base-url:}") String configuredLocalBaseUrl,
            ObjectProvider<S3Client> s3ClientProvider) throws IOException {
        this.awsStorageProperties = awsStorageProperties;
        this.localBasePath = Paths.get(localBasePath).toAbsolutePath().normalize();
        Files.createDirectories(this.localBasePath);
        this.localUploadsPrefix = normalizePrefix(localPublicPrefix);
        this.localBaseUrl = trimTrailingSlash(configuredLocalBaseUrl);
        this.s3Client = s3ClientProvider.getIfAvailable();
    }

    public Optional<String> storeSeedMarkdown(Long blogId, Path source) {
        return storeSeedAsset(blogId, source, "blog.md", "text/markdown; charset=UTF-8");
    }

    public Optional<String> storeSeedImage(Long blogId, Path source) {
        return storeSeedAsset(blogId, source, "blog.jpg", "image/jpeg");
    }

    public String storeMarkdown(Long blogId, MultipartFile markdown) throws IOException {
        requireBlogId(blogId);
        if (markdown == null || markdown.isEmpty()) {
            throw new IllegalArgumentException("El archivo de contenido es obligatorio");
        }
        return storeAssetFromStream(blogId, markdown::getInputStream, markdown.getSize(), "blog.md",
                "text/markdown; charset=UTF-8");
    }

    public String storeImage(Long blogId, MultipartFile imagen) throws IOException {
        requireBlogId(blogId);
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es obligatorio");
        }
        String extension = resolveExtension(imagen.getOriginalFilename(), imagen.getContentType());
        String fileName = "blog." + extension;
        String contentType = determineImageContentType(imagen.getContentType(), extension);
        return storeAssetFromStream(blogId, imagen::getInputStream, imagen.getSize(), fileName, contentType);
    }

    private Optional<String> storeSeedAsset(Long blogId, Path source, String fileName, String contentType) {
        requireBlogId(blogId);
        if (source == null || !Files.exists(source)) {
            return Optional.empty();
        }
        try {
            if (useS3Storage()) {
                long size = Files.size(source);
                try (InputStream inputStream = Files.newInputStream(source)) {
                    String url = storeInS3(blogId, inputStream, size, fileName, contentType);
                    return Optional.of(url);
                }
            }
            Path target = buildLocalPath(blogId, fileName);
            Files.createDirectories(target.getParent());
            if (Files.exists(target) && Files.isSameFile(source, target)) {
                return Optional.of(buildLocalUrl(blogId, fileName));
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(buildLocalUrl(blogId, fileName));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo sincronizar el asset del blog " + blogId, ex);
        }
    }

    private String storeAssetFromStream(Long blogId, IOStreamSupplier supplier, long size, String fileName,
            String contentType) throws IOException {
        if (useS3Storage()) {
            try (InputStream inputStream = supplier.openStream()) {
                return storeInS3(blogId, inputStream, size, fileName, contentType);
            }
        }
        Path target = buildLocalPath(blogId, fileName);
        Files.createDirectories(target.getParent());
        try (InputStream inputStream = supplier.openStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return buildLocalUrl(blogId, fileName);
    }

    private String storeInS3(Long blogId, InputStream inputStream, long size, String fileName, String contentType)
            throws IOException {
        if (!awsStorageProperties.hasBucketConfigured() || s3Client == null) {
            throw new IllegalStateException("No hay un bucket S3 configurado para almacenar blogs");
        }
        String key = BLOG_FOLDER + "/" + blogId + "/" + fileName;
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(awsStorageProperties.getBucketName())
                .key(key);
        if (StringUtils.hasText(contentType)) {
            requestBuilder.contentType(contentType);
        }
        try {
            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(inputStream, size));
        } catch (Exception ex) {
            throw new IOException("Error al subir el archivo a S3", ex);
        }
        return buildS3Url(key);
    }

    private boolean useS3Storage() {
        return awsStorageProperties.hasBucketConfigured() && s3Client != null;
    }

    private Path buildLocalPath(Long blogId, String fileName) {
        return localBasePath.resolve(BLOG_FOLDER)
                .resolve(String.valueOf(blogId))
                .resolve(fileName);
    }

    private String buildLocalUrl(Long blogId, String fileName) {
        String prefix = StringUtils.hasText(localBaseUrl) ? localBaseUrl + localUploadsPrefix : localUploadsPrefix;
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + BLOG_FOLDER + "/" + blogId + "/" + fileName;
    }

    private String buildS3Url(String key) {
        String bucketUrl = awsStorageProperties.getBucketUrl();
        if (StringUtils.hasText(bucketUrl)) {
            String normalized = bucketUrl.endsWith("/") ? bucketUrl : bucketUrl + "/";
            return normalized + key;
        }
        return "https://" + awsStorageProperties.getBucketName() + ".s3.amazonaws.com/" + key;
    }

    private void requireBlogId(Long blogId) {
        if (blogId == null || blogId <= 0) {
            throw new IllegalArgumentException("El blog debe tener un ID antes de adjuntar archivos");
        }
    }

    private String resolveExtension(String originalFileName, String contentType) {
        String extension = extractExtension(originalFileName);
        if (StringUtils.hasText(extension)) {
            return extension.toLowerCase(Locale.ROOT);
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return "gif";
        }
        return "jpg";
    }

    private String determineImageContentType(String provided, String extension) {
        if (StringUtils.hasText(provided)) {
            return provided;
        }
        return switch (extension) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        String normalized = Paths.get(fileName).getFileName().toString();
        int dot = normalized.lastIndexOf('.');
        if (dot <= 0 || dot == normalized.length() - 1) {
            return null;
        }
        return normalized.substring(dot + 1);
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "/";
        }
        String normalized = prefix;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    @FunctionalInterface
    private interface IOStreamSupplier {
        InputStream openStream() throws IOException;
    }
}
