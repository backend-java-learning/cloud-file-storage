package com.example.config;

import com.example.service.storage.DirectoryResourceService;
import com.example.service.DirectoryService;
import com.example.utils.ZipCreator;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class AppConfig {

    private DirectoryResourceService directoryService;

    @Bean
    public DirectoryService directoryService() {
        return directoryService;
    }
}
