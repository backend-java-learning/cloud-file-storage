package com.example.config;

import com.example.service.minio.MinioArgsFactory;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;


    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public String bucket() {
        return bucketName;
    }

    @Bean
    public MinioArgsFactory minioArgsFactory() {
        return new MinioArgsFactory(bucketName);
    }
}
