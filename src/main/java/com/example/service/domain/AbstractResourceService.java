package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.FileMetadata;
import com.example.models.StorageKey;
import com.example.service.FileMetadataService;
import com.example.service.ResourceService;
import com.example.service.StorageService;
import io.minio.StatObjectResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractResourceService implements ResourceService {

    protected final StorageService storageService;
    protected final FileMetadataService fileMetadataService;
    protected final ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        Optional<FileMetadata> fileMetadataOptional = fileMetadataService.findByStorageKey(storageKey);
        if (fileMetadataOptional.isEmpty()) {
            log.error("Resource [{}] doesn't exist", storageKey.buildKey());
            throw new ResourceNotFoundException("Resource [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        return resourceInfoMapper.toDto(fileMetadataOptional.get());
    }

    @Override
    public List<ResourceInfoResponse> upload(StorageKey storageKey, List<MultipartFile> files) {
        List<ResourceInfoResponse> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            ResourceInfoResponse uploadedFile = upload(storageKey, file);
            uploadedFiles.add(uploadedFile);
        }
        return uploadedFiles;
    }

    @Transactional
    private ResourceInfoResponse upload(StorageKey storageKey, MultipartFile file) {
        storageService.putObject(storageKey, file);
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        StorageKey statObjectStorageKey = StorageKey.parsePath(statObjectResponse.object());
        fileMetadataService.save(statObjectStorageKey, statObjectResponse.size());
        return resourceInfoMapper.toDto(statObjectStorageKey, statObjectResponse.size());
    }
}
