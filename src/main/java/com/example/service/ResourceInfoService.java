package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ResourceInfoService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    public ResourceInfoResponse getResourceInfo(String bucket, String resourceName, int userId) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException,
            InvalidResponseException, XmlParserException, InternalException {
        resourceName = "user-%s-files/%s".formatted(userId, resourceName);
        return resourceName.endsWith("/")
                ? getDirectoryInfo(bucket, resourceName)
                : getFileInfo(bucket, resourceName);
    }

    public void deleteResource(String bucket, String resourceName, int userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String key = "user-%s-files/%s".formatted(userId, resourceName);
        if(resourceName.endsWith("/")) {
            deleteObjects(bucket, key);
            return;
        }
        deleteObject(bucket, key);
    }

    private void deleteObjects(String bucket, String key) {
        storageService.removeObjects(bucket, key);
    }

    private void deleteObject(String bucket, String key) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        storageService.removeObject(bucket, key);
    }

    private ResourceInfoResponse getDirectoryInfo(String bucket, String folderName) {
        List<Result<Item>> results = storageService.getListObjects(bucket, folderName);
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

    private ResourceInfoResponse getFileInfo(String bucket, String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        StatObjectResponse statObjectResponse = storageService.getStatObject(bucket, fileName);
        String fileNameWithPath = statObjectResponse.object();
        int lastIndexOfSplitter = fileNameWithPath.lastIndexOf("/");
        String folderName = fileNameWithPath.substring(0, lastIndexOfSplitter + 1);
        String fileName1 = fileNameWithPath.substring(lastIndexOfSplitter + 1);
        return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), folderName, fileName1, ResourceType.FILE);
    }
}
