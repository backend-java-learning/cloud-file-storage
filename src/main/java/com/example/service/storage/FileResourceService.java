package com.example.service.storage;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoDto;
import com.example.dto.enums.ResourceType;
import com.example.exception.resource.ResourceAlreadyExist;
import com.example.exception.resource.ResourceException;
import com.example.exception.resource.ResourceTypeException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.minio.StorageService;
import io.minio.StatObjectResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

@Slf4j
@Service
public class FileResourceService extends AbstractResourceService {

    public FileResourceService(StorageService storageService,
                               ResourceInfoMapper resourceInfoMapper) {
        super(storageService, resourceInfoMapper);
    }

    @Override
    public ResourceInfoDto getInfo(StorageKey storageKey) {
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        StorageKey statObjectStorageKey = StorageKey.parsePath(statObjectResponse.object());
        return resourceInfoMapper.toResourceInfoDto(statObjectStorageKey, statObjectResponse.size());
    }

    @Transactional(rollbackOn = Exception.class)
    @Override
    public ResourceInfoDto move(StorageKey sourcePrefix, StorageKey targetPrefix) {
        if (sourcePrefix.getResourceType() != targetPrefix.getResourceType()) {
            throw new ResourceTypeException("Source and target key must have the same resource type [FILE | DIRECTORY]");
        }
        validateFile(sourcePrefix);
        if (storageService.doesObjectExist(targetPrefix)) {
            throw new ResourceAlreadyExist("Move file exception: the file [%s] already exist".formatted(targetPrefix.getPath()));
        }
        storageService.moveObject(sourcePrefix, targetPrefix);
        return getInfo(targetPrefix);
    }

    @Override
    public DownloadResult downloadStream(StorageKey storageKey) {
        validateFile(storageKey);
        StreamingResponseBody body = out -> {
            try (InputStream is = storageService.getObject(storageKey)) {
                is.transferTo(out);
            }
        };
        return new DownloadResult(storageKey.getObjectName(), body, "application/octet-stream");
    }

    @Override
    public void remove(StorageKey storageKey) {
        validateFile(storageKey);
        storageService.removeObject(storageKey);
    }

    private void validateFile(StorageKey storageKey) {
        validateResource(storageKey, ResourceType.FILE);
    }
}
