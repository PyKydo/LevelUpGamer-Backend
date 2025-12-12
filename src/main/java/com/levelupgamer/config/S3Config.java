package com.levelupgamer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3Config {

    private final AwsStorageProperties awsStorageProperties;

    public S3Config(AwsStorageProperties awsStorageProperties) {
        this.awsStorageProperties = awsStorageProperties;
    }

    @Bean
    public S3Client s3Client() {
        String region = awsStorageProperties.getRegion();
        String resolvedRegion = StringUtils.hasText(region) ? region : "us-east-1";
        return S3Client.builder()
            .region(Region.of(resolvedRegion))
            .build();
    }
}
