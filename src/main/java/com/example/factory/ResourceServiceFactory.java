package com.example.factory;

import com.example.dto.enums.ResourceType;
import com.example.service.storage.DirectoryResourceService;
import com.example.service.storage.FileResourceService;
import com.example.service.ResourceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ResourceServiceFactory {

    private FileResourceService fileService;
    private DirectoryResourceService directoryService;

    public ResourceService create(ResourceType type) {
        return switch (type) {
            case FILE -> fileService;
            case DIRECTORY -> directoryService;
        };
    }
}
