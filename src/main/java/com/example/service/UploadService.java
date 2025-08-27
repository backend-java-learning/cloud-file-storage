package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@AllArgsConstructor
public class UploadService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    public List<ResourceInfoResponse> uploadFile(int userId, String folderPath, MultipartFile file) {
        if (!storageService.doesObjectExist(userId, folderPath)) {
            storageService.createEmptyFolder(userId, folderPath);
        }
        storageService.putObject(userId, folderPath, file);
        StatObjectResponse statObjectResponse = storageService.getStatObject(userId, folderPath);
        ResourceInfoResponse resourceInfoResponse = resourceInfoMapper
                .toResourceInfo(statObjectResponse.size(), folderPath, file.getOriginalFilename(), ResourceType.FILE);
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
