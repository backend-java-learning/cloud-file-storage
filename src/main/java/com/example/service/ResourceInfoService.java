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

    public ResourceInfoResponse getResourceInfo(String resourceName, int userId) {
        return resourceName.endsWith("/")
                ? getDirectoryInfo(userId, resourceName)
                : getFileInfo(userId, resourceName);
    }

    public void deleteResource(String resourceName, int userId) {
        if (resourceName.endsWith("/")) {
            deleteObjects(userId, resourceName);
            return;
        }
        deleteObject(userId, resourceName);
    }

    private void deleteObjects(int userId, String key) {
        storageService.removeObjects(userId, key);
    }

    private void deleteObject(int userId, String key) {
        storageService.removeObject(userId, key);
    }

    private ResourceInfoResponse getDirectoryInfo(int userId, String folderName) {
        if (!storageService.doesObjectExist(userId, folderName)) {
            log.error("Directory [{}] doesn't exist", folderName);
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

    private ResourceInfoResponse getFileInfo(int userId, String fileName) {
        StatObjectResponse statObjectResponse = storageService.getStatObject(userId, fileName);
        String fileNameWithPath = statObjectResponse.object();
        int lastIndexOfSplitter = fileNameWithPath.lastIndexOf("/");
        String folderName = fileNameWithPath.substring(0, lastIndexOfSplitter + 1);
        String fileName1 = fileNameWithPath.substring(lastIndexOfSplitter + 1);
        return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), folderName, fileName1, ResourceType.FILE);
    }
}
