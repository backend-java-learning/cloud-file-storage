package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.domain.DirectoryService;
import com.example.service.domain.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ResourceInfoService {

    private StorageService storageService;
    private FileService fileService;
    private DirectoryService directoryService;

    public ResourceInfoResponse getResourceInfo(String resourceName, int userId) {
        StorageKey storageKey = new StorageKey(userId, resourceName);
        return getResourceInfo(storageKey);
    }

    public ResourceInfoResponse getResourceInfo(StorageKey storageKey) {
        return storageKey.relativePath().endsWith("/")
                ? directoryService.getInfo(storageKey)
                : fileService.getInfo(storageKey);
    }

    public void deleteResource(int userId, String resourceName) {
        StorageKey storageKey = new StorageKey(userId, resourceName);
        if (resourceName.endsWith("/")) {
            deleteObjects(storageKey);
            return;
        }
        deleteObject(storageKey);
    }

    private void deleteObjects(StorageKey storageKey) {
        storageService.removeObjects(storageKey);
    }

    private void deleteObject(StorageKey storageKey) {
        storageService.removeObject(storageKey);
    }
}
