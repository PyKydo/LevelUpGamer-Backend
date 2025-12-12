package com.levelupgamer.common.storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.levelupgamer.config.AwsStorageProperties;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements FileStorageService {

    private static final String DEFAULT_FOLDER = "uploads";
    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(S3Client s3Client, AwsStorageProperties awsStorageProperties) {
        this.s3Client = s3Client;
        String configuredBucket = awsStorageProperties.getBucketName();
        if (!StringUtils.hasText(configuredBucket)) {
            throw new IllegalStateException("aws.s3.bucket.name es obligatorio cuando storage.provider=s3");
        }
        this.bucketName = configuredBucket;
    }

    @Override
    public List<String> listPublicUrls(String folder) throws IOException {
        String sanitizedFolder = StorageKeyUtils.sanitizeFolder(folder, folder);
        String prefix = sanitizedFolder.endsWith("/") ? sanitizedFolder : sanitizedFolder + "/";
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .build();
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            return response.contents().stream()
                    .map(S3Object::key)
                    .filter(key -> !key.endsWith("/"))
                    .map(key -> s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm())
                    .toList();
        } catch (Exception ex) {
            throw new IOException("Error al listar objetos S3 para la carpeta " + sanitizedFolder, ex);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFileName, long contentLength, String folder,
            String contentType) throws IOException {
        String targetFolder = StorageKeyUtils.sanitizeFolder(folder, DEFAULT_FOLDER);
        String sanitizedName = StorageKeyUtils.sanitizeFileName(originalFileName);
        String key = targetFolder + "/" + StorageKeyUtils.shortUuid() + "_" + sanitizedName;

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key);

        if (StringUtils.hasText(contentType)) {
            requestBuilder.contentType(contentType);
        }

        s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(inputStream, contentLength));
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

    @Override
    public boolean deleteIfManaged(String publicUrl) throws IOException {
        String key = resolveKey(publicUrl);
        if (key == null) {
            return false;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(request);
            return true;
        } catch (Exception ex) {
            throw new IOException("Error al eliminar el objeto S3: " + ex.getMessage(), ex);
        }
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
