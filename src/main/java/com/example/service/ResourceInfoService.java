package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;
import com.example.service.domain.DirectoryService;
import com.example.service.domain.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ResourceInfoService {

    private FileService fileService;
    private DirectoryService directoryService;

    public ResourceInfoResponse getResourceInfo(StorageKey storageKey) {
        return storageKey.relativePath().endsWith("/")
                ? directoryService.getInfo(storageKey)
                : fileService.getInfo(storageKey);
    }
}
