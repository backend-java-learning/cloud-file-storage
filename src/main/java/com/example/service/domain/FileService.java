package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.ResourcePath;
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
        String fileNameWithPath = statObjectResponse.object().replace(storageKey.getPrefix(), "");
        ResourcePath resourcePath = ResourcePath.of(fileNameWithPath);
        return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), resourcePath.getParentPath(),
                resourcePath.getFolderName(), ResourceType.FILE);
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
