package com.picturebook.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }

    @Bean
    public MinioClient publicMinioClient(MinioProperties properties) {
        String publicEndpoint = properties.publicEndpoint();
        if (publicEndpoint == null || publicEndpoint.isBlank()) {
            publicEndpoint = properties.endpoint();
        }
        return MinioClient.builder()
                .endpoint(publicEndpoint)
                .credentials(properties.accessKey(), properties.secretKey())
                .region("us-east-1")
                .build();
    }
}
