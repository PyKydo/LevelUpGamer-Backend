package com.levelupgamer.config;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


@Component
@Profile("!test")
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3SeedSynchronizationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(S3SeedSynchronizationRunner.class);
    private static final List<String> DEFAULT_FOLDERS = List.of("blogs", "products");

    private final S3Client s3Client;
    private final AwsStorageProperties awsStorageProperties;
    private final Path seedBasePath;
    private final boolean syncEnabled;

    public S3SeedSynchronizationRunner(
            S3Client s3Client,
            AwsStorageProperties awsStorageProperties,
            @Value("${aws.seed.local-base-dir:${storage.local.base-path:${user.dir}/s3-files}}") String seedBaseDir,
            @Value("${aws.seed.sync-enabled:true}") boolean syncEnabled) {
        this.s3Client = s3Client;
        this.awsStorageProperties = awsStorageProperties;
        this.seedBasePath = Paths.get(seedBaseDir).toAbsolutePath().normalize();
        this.syncEnabled = syncEnabled;
    }

    @Override
    public void run(String... args) {
        if (!syncEnabled) {
            log.info("Sync apagado por configuracion");
            return;
        }
        if (!awsStorageProperties.hasBucketConfigured()) {
            log.info("Sin bucket configurado, no se sincroniza");
            return;
        }
        if (!Files.exists(seedBasePath)) {
            log.info("Ruta local {} no existe, salto sync", seedBasePath);
            return;
        }

        String bucketName = awsStorageProperties.getBucketName();
        DEFAULT_FOLDERS.forEach(folder -> syncFolder(folder, bucketName));
    }

    private void syncFolder(String folder, String bucketName) {
        Path folderPath = seedBasePath.resolve(folder);
        if (!Files.exists(folderPath)) {
            log.debug("Carpeta {} no existe, salto", folderPath);
            return;
        }

        try (Stream<Path> files = Files.walk(folderPath, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isSyncCandidate)
                    .forEach(path -> uploadIfMissing(path, bucketName));
        } catch (IOException ex) {
            log.warn("Error recorriendo {}: {}", folderPath, ex.getMessage());
        }
    }

    private boolean isSyncCandidate(Path path) {
        String fileName = path.getFileName().toString();
        return !fileName.startsWith(".");
    }

    private void uploadIfMissing(Path file, String bucketName) {
        String key = seedBasePath.relativize(file).toString().replace("\\", "/");
        if (objectExists(bucketName, key)) {
            return;
        }

        try {
            String contentType = Files.probeContentType(file);
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);
            if (contentType != null && !contentType.isBlank()) {
                requestBuilder.contentType(contentType);
            }
            s3Client.putObject(requestBuilder.build(), RequestBody.fromFile(file));
            log.info("Archivo {} subido a bucket {}", key, bucketName);
        } catch (S3Exception | IOException ex) {
            log.warn("No se pudo subir {}: {}", key, ex.getMessage());
        }
    }

    private boolean objectExists(String bucketName, String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw ex;
        }
    }
}
