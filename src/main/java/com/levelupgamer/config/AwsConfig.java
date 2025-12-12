package com.levelupgamer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(AwsStorageProperties.class)
public class AwsConfig {

	@Bean
	@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
	public S3Client s3Client(AwsStorageProperties awsStorageProperties) {
		Region region = Region.of(awsStorageProperties.getRegion());
		return S3Client.builder()
				.region(region)
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
