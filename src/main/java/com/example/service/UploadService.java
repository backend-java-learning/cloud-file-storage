package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class UploadService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    public List<ResourceInfoResponse> uploadFile(int userId, String objectName, MultipartFile file) {
        StorageKey storageKey = new StorageKey(userId, objectName);
        if (!storageService.doesObjectExist(storageKey)) {
            storageService.putEmptyFolder(storageKey);
        }
        storageService.putObject(storageKey, file);
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        ResourceInfoResponse resourceInfoResponse = resourceInfoMapper
                .toResourceInfo(statObjectResponse.size(), storageKey.relativePath(), file.getOriginalFilename(), ResourceType.FILE);
        return List.of(resourceInfoResponse);
    }

//    public void uploadFiles(int userId, String bucket, String basePath, List<MultipartFile> files) {
//        for (MultipartFile file : files) {
//            String relativePath = file.getOriginalFilename();
//            String key = "%s/%s".formatted(basePath, relativePath);
//            storageService.putObject(userId, bucket, key, file);
//        }
//    }
}
