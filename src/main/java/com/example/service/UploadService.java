package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.mapper.ResourceInfoMapper;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@AllArgsConstructor
public class UploadService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    public List<ResourceInfoResponse> uploadFile(String bucket, String folderPath, MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String key = "%s/%s".formatted(folderPath, file.getOriginalFilename());
        storageService.putObject(bucket, key, file);
        StatObjectResponse statObjectResponse = storageService.getStatObject(bucket, key);
        ResourceInfoResponse resourceInfoResponse = resourceInfoMapper
                .toResourceInfo(statObjectResponse.size(), folderPath, file.getOriginalFilename(), ResourceType.FILE);
        return List.of(resourceInfoResponse);
    }

    public void uploadFiles(String bucket, String basePath, List<MultipartFile> files) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (MultipartFile file : files) {
            String relativePath = file.getOriginalFilename();
            String key = "%s/%s".formatted(basePath, relativePath);
            storageService.putObject(bucket, key, file);
        }
    }
}
