package com.example.service;

import com.example.dto.ResourceInfoResponse;
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

    public List<ResourceInfoResponse> uploadFile(StorageKey storageKey, MultipartFile file) {
        storageService.putObject(storageKey, file);
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        StorageKey statObjectStorageKey = StorageKey.parsePath(statObjectResponse.object());
        ResourceInfoResponse resourceInfoResponse = resourceInfoMapper.toResourceInfo(statObjectStorageKey, statObjectResponse.size());
        return List.of(resourceInfoResponse);
    }

//    public void uploadFiles(int userId, String bucket, String basePath, MultipartFile files) {
//        for (MultipartFile file : files) {
//            String relativePath = file.getOriginalFilename();
//            String key = "%s/%s".formatted(basePath, relativePath);
//            storageService.putObject(userId, bucket, key, file);
//        }
//    }
}
