package com.example.factory;

import com.example.dto.enums.ResourceType;
import com.example.service.domain.DirectoryService;
import com.example.service.domain.FileService;
import com.example.service.domain.ResourceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ResourceServiceFactory {

    private FileService fileService;
    private DirectoryService directoryService;

    public ResourceService getService(ResourceType type) {
        return switch (type) {
            case FILE -> fileService;
            case DIRECTORY -> directoryService;
        };
    }
}
