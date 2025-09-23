package com.example.service.storage;

import com.example.dto.ResourceInfoDto;
import com.example.dto.enums.ResourceType;
import com.example.exception.resource.ResourceException;
import com.example.exception.resource.ResourceTypeException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.ResourceService;
import com.example.service.minio.StorageService;
import io.minio.StatObjectResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractResourceService implements ResourceService {

    protected final StorageService storageService;
    protected final ResourceInfoMapper resourceInfoMapper;

    @Override
    public List<ResourceInfoDto> upload(StorageKey storageKey, List<MultipartFile> files) {
        List<ResourceInfoDto> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            ResourceInfoDto uploadedFile = upload(storageKey, file);
            uploadedFiles.add(uploadedFile);
        }
        return uploadedFiles;
    }

    @Transactional
    private ResourceInfoDto upload(StorageKey storageKey, MultipartFile file) {
        String newPath = storageKey.buildKey() + file.getOriginalFilename();
        StorageKey newFile = StorageKey.parsePath(newPath);
        if (storageService.doesObjectExist(newFile)) {
            throw new ResourceException("Couldn't upload file, because file with path [%s] and name [%s] already exist".formatted(newFile.getPath(), newFile.getObjectName()));
        }
        storageService.putObject(newFile, file);
        StatObjectResponse statObjectResponse = storageService.getStatObject(newFile);
        StorageKey statObjectStorageKey = StorageKey.parsePath(statObjectResponse.object());
        return resourceInfoMapper.toResourceInfoDto(statObjectStorageKey, statObjectResponse.size());
    }

    protected void validateResource(StorageKey storageKey, ResourceType expectedResourceType) {
        if (storageKey.getResourceType() != expectedResourceType) {
            throw new ResourceTypeException("Wrong storage key type, expected type is [%s]".formatted(expectedResourceType));
        }
    }
}
