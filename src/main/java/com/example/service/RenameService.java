package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RenameService {

    private StorageService storageService;
    private ResourceInfoService resourceInfoService;

    public ResourceInfoResponse moveResource(int userId, String sourceKey, String targetKey) {
        StorageKey sourceStorageKey = new StorageKey(userId, sourceKey);
        StorageKey targetStorageKey = new StorageKey(userId, targetKey);
        return sourceKey.endsWith("/")
                ? moveFolder(sourceStorageKey, targetStorageKey)
                : moveFile(sourceStorageKey, targetStorageKey);
    }

    private ResourceInfoResponse moveFolder(StorageKey sourcePrefix, StorageKey targetPrefix) {
        List<String> results = storageService.getObjectsNames(sourcePrefix, true);
        for (String sourceKey : results) {
            var LastIndexOf = sourceKey.lastIndexOf(sourcePrefix.relativePath());
            String relativePath = sourceKey.substring(LastIndexOf + sourcePrefix.relativePath().length());
            String targetKey = targetPrefix + relativePath;
            StorageKey sourceStorageKey = new StorageKey(sourcePrefix.userId(), sourceKey);
            StorageKey targetStorageKey = new StorageKey(targetPrefix.userId(), targetKey);
            moveObject(sourceStorageKey, targetStorageKey);
        }
        return resourceInfoService.getResourceInfo(targetPrefix);
    }

    private ResourceInfoResponse moveFile(StorageKey targetStorageKey, StorageKey sourceStorageKey) {
        moveObject(targetStorageKey, sourceStorageKey);
        return resourceInfoService.getResourceInfo(targetStorageKey);
    }

    private void moveObject(StorageKey targetStorageKey, StorageKey sourceStorageKey) {
        storageService.copyObject(targetStorageKey, sourceStorageKey);
        storageService.removeObject(sourceStorageKey);
    }
}
