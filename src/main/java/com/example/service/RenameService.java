package com.example.service;

import com.example.dto.ResourceInfoResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RenameService {

    private StorageService storageService;
    private ResourceInfoService resourceInfoService;

    public ResourceInfoResponse moveResource(int userId, String sourceKey, String targetKey) {
        return sourceKey.endsWith("/")
                ? moveFolder(userId, sourceKey, targetKey)
                : moveFile(userId, sourceKey, targetKey);
    }

    private ResourceInfoResponse moveFolder(int userId, String sourcePrefix, String targetPrefix) {
        //TODO: think about exceptions
        if (!sourcePrefix.endsWith("/")) {
            sourcePrefix += "/";
        }
        if (!targetPrefix.endsWith("/")) {
            targetPrefix += "/";
        }
        List<String> results = storageService.getObjectsNames(userId, sourcePrefix, true);
        for (String sourceKey : results) {
            var LastIndexOf = sourceKey.lastIndexOf(sourcePrefix);
            String relativePath = sourceKey.substring(LastIndexOf + sourcePrefix.length());
            String targetKey = targetPrefix + relativePath;
            moveObject(userId, sourceKey, targetKey);
        }
        return resourceInfoService.getResourceInfo(targetPrefix, userId);
    }

    private ResourceInfoResponse moveFile(int userId, String sourceKey, String targetKey) {
        moveObject(userId, sourceKey, targetKey);
        return resourceInfoService.getResourceInfo(targetKey, userId);
    }

    private void moveObject(int userId, String sourceKey, String targetKey) {
        storageService.copyObject(userId, targetKey, sourceKey);
        storageService.removeObject(userId, sourceKey);
    }
}
