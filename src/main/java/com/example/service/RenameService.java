package com.example.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RenameService {

    private StorageService storageService;

    // public void moveResource(String bucket, int userId)

    public void moveObject(int userId, String sourceKey, String targetKey) {
        storageService.copyObject(userId, sourceKey, targetKey);
        storageService.removeObject(userId, sourceKey);
    }

    public void moveFolder(int userId, String sourcePrefix, String targetPrefix) {
        if (!sourcePrefix.endsWith("/")) {
            sourcePrefix += "/";
        }
        if (!targetPrefix.endsWith("/")) {
            targetPrefix += "/";
        }
        List<String> results = storageService.getObjectsNames(userId, sourcePrefix, true);
        for (String sourceKey : results) {
            String relativePath = sourceKey.substring(sourcePrefix.length());
            String targetKey = targetPrefix + relativePath;
            moveObject(userId, targetKey, sourceKey);
        }
    }
}
