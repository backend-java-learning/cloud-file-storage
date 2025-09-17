package com.example.config;

import com.example.utils.ZipCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZipConfig {

    @Bean
    public ZipCreator zipArchiveCreator() {
        return new ZipCreator();
    }
}
