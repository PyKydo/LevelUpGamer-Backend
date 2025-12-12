package com.levelupgamer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "aws")
public class AwsStorageProperties {

    private String region = "us-east-1";
    private final S3 s3 = new S3();

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public S3 getS3() {
        return s3;
    }

    public String getBucketName() {
        return s3.getBucket().getName();
    }

    public String getBucketUrl() {
        return s3.getBucket().getUrl();
    }

    public boolean hasBucketConfigured() {
        return StringUtils.hasText(getBucketName());
    }

    public static class S3 {
        private final Bucket bucket = new Bucket();

        public Bucket getBucket() {
            return bucket;
        }
    }

    public static class Bucket {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
