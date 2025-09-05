package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.StorageService;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FileService implements ResourceService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        String fileNameWithPath = statObjectResponse.object();
        int lastIndexOfSplitter = fileNameWithPath.lastIndexOf("/");
        String folderName = fileNameWithPath.substring(0, lastIndexOfSplitter + 1);
        String fileName1 = fileNameWithPath.substring(lastIndexOfSplitter + 1);
        return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), folderName, fileName1, ResourceType.FILE);
    }

    @Override
    public void remove() {

    }

    @Override
    public void download() {

    }

    @Override
    public ResourceInfoResponse rename() {
        return null;
    }
}
