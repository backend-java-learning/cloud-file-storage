package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.ResourcePath;
import com.example.models.StorageKey;
import com.example.service.StorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DirectoryService implements ResourceService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        if (!storageService.doesObjectExist(storageKey)) {
            log.error("Directory [{}] doesn't exist", storageKey.buildKey());
            throw new ResourceNotFoundException("Directory [%s] doesn't exist".formatted(storageKey.relativePath()));
        }
        ResourcePath folderPath = ResourcePath.of(storageKey.relativePath());
        return resourceInfoMapper.toResourceInfo(folderPath.getParentPath(), folderPath.getFolderName(), ResourceType.DIRECTORY);
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
