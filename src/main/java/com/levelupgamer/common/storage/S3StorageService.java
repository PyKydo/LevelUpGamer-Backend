package com.levelupgamer.common.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements FileStorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(S3Client s3Client,
            @Value("${aws.s3.bucket.name}") String bucketName) {
        this.s3Client = s3Client;
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("aws.s3.bucket.name es obligatorio cuando storage.provider=s3");
        }
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFileName, long contentLength) throws IOException {
        String safeName = StringUtils.hasText(originalFileName) ? originalFileName : "asset";
        String key = "uploads/" + UUID.randomUUID() + "-" + safeName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm();
    }

    @Override
    public Optional<String> readContentIfManaged(String publicUrl) throws IOException {
        String key = resolveKey(publicUrl);
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(getFileContent(key));
    }

    private String getFileContent(String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = s3Object.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error al leer el objeto S3: " + e.getMessage(), e);
        }
    }

    private String resolveKey(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            String path = uri.getPath();

            if (host != null && host.contains(bucketName)) {
                return trimLeadingSlash(path);
            }

            if (host != null && host.contains("s3.amazonaws.com")) {
                String p = trimLeadingSlash(path);
                String bucketPrefix = bucketName + "/";
                if (p != null && p.startsWith(bucketPrefix)) {
                    return p.substring(bucketPrefix.length());
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Si la URL no es válida, se ignorará y se devolverá null
        }
        return null;
    }

    private String trimLeadingSlash(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
