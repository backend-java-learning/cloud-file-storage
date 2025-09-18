package com.example.config;

import com.example.service.minio.MinioArgsFactory;
import com.example.utils.ZipCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZipConfig {

    @Bean
    public ZipCreator zipArchiveCreator() {
        return new ZipCreator();
    }

    @Bean
    public MinioArgsFactory minioArgsFactory() {
        return new MinioArgsFactory();
    }
}
