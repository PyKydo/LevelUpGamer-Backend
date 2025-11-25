package com.levelupgamer.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.provider:local}")
    private String storageProvider;

    @Value("${storage.local.base-path:}")
    private String localBasePath;

    @Value("${storage.local.public-url-prefix:/uploads/}")
    private String localPublicPrefix;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        if (!"s3".equalsIgnoreCase(storageProvider) && StringUtils.hasText(localBasePath)) {
            String normalizedPrefix = normalizePrefix(localPublicPrefix) + "**";
            String location = Paths.get(localBasePath).toAbsolutePath().normalize().toUri().toString();
            registry.addResourceHandler(normalizedPrefix)
                    .addResourceLocations(location);
        }
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
