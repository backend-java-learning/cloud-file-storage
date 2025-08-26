package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ResourceInfoService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    public ResourceInfoResponse getResourceInfo(String bucket, String resourceName, int userId) {
        return resourceName.endsWith("/")
                ? getDirectoryInfo(userId, bucket, resourceName)
                : getFileInfo(userId, bucket, resourceName);
    }

    public void deleteResource(String bucket, String resourceName, int userId) {
        if (resourceName.endsWith("/")) {
            deleteObjects(userId, bucket, resourceName);
            return;
        }
        deleteObject(userId, bucket, resourceName);
    }

    private void deleteObjects(int userId, String bucket, String key) {
        storageService.removeObjects(userId, bucket, key);
    }

    private void deleteObject(int userId, String bucket, String key) {
        storageService.removeObject(userId, bucket, key);
    }

    private ResourceInfoResponse getDirectoryInfo(int userId, String bucket, String folderName) {
        List<Result<Item>> results = storageService.getListObjects(userId, bucket, folderName);
        if (results.isEmpty()) {
            log.error("Directory [{}] doesn't exist in bucket [{}]", folderName, bucket);
            throw new ResourceNotFoundException("Directory doesn't exist");
        }

        String trimmed = folderName.endsWith("/")
                ? folderName.substring(0, folderName.length() - 1)
                : folderName;
        int lastSlash = trimmed.lastIndexOf("/");

        //TODO: move in utils
        String path;
        String name;

        if (lastSlash == -1) {
            // Если слешей нет, значит путь пустой, а всё остальное — имя
            path = "";
            name = folderName;
        } else {
            path = folderName.substring(0, lastSlash + 1); // включая последний "/"
            name = folderName.substring(lastSlash + 1);    // имя без "/"

            // Добавляем "/" к имени, если в исходном пути он был
            if (folderName.endsWith("/")) {
                name = name + "/";
            }
        }

        return resourceInfoMapper.toResourceInfo(path, name, ResourceType.DIRECTORY);
    }

    private ResourceInfoResponse getFileInfo(int userId, String bucket, String fileName) {
        StatObjectResponse statObjectResponse = storageService.getStatObject(userId, bucket, fileName);
        String fileNameWithPath = statObjectResponse.object();
        int lastIndexOfSplitter = fileNameWithPath.lastIndexOf("/");
        String folderName = fileNameWithPath.substring(0, lastIndexOfSplitter + 1);
        String fileName1 = fileNameWithPath.substring(lastIndexOfSplitter + 1);
        return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), folderName, fileName1, ResourceType.FILE);
    }
}
