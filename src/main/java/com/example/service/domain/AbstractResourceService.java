package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.ResourceService;
import com.example.service.StorageService;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class AbstractResourceService implements ResourceService {

    protected StorageService storageService;
    protected ResourceInfoMapper resourceInfoMapper;

    @Override
    public List<ResourceInfoResponse> upload(StorageKey storageKey, List<MultipartFile> files) {
        List<ResourceInfoResponse> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            storageService.putObject(storageKey, file);
            StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
            StorageKey statObjectStorageKey = StorageKey.parsePath(statObjectResponse.object());
            uploadedFiles.add(resourceInfoMapper.toResourceInfo(statObjectStorageKey, statObjectResponse.size()));
        }
        return uploadedFiles;
    }
}
