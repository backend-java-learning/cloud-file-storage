package com.example.service;

import com.example.dto.ResourceInfoDto;
import com.example.exception.resource.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.ResourceInfo;
import com.example.models.StorageKey;
import com.example.repository.FileMetadataRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;
    private final ResourceInfoMapper resourceInfoMapper;

    @Transactional
    public void updateFileMetadata(StorageKey oldKey, StorageKey newKey) {
        ResourceInfo resourceInfo = fileMetadataRepository.findByKeyAndPathAndName(oldKey.getKey(), oldKey.getPrefix(),
                        oldKey.getObjectName())
                .orElseThrow(() -> new ResourceNotFoundException("Object doesn't exist"));
        resourceInfo.setKey(newKey.getKey());
        resourceInfo.setPath(newKey.getPrefix());
        resourceInfo.setName(newKey.getObjectName());
        resourceInfo.setType(newKey.getResourceType());
    }

    public Optional<ResourceInfo> findByStorageKey(StorageKey storageKey) {
        ResourceInfo resourceInfo = resourceInfoMapper.toResourceInfo(storageKey);
        //TODO: add validation
        return fileMetadataRepository.findOne(Example.of(resourceInfo));
    }

    public Optional<ResourceInfo> findOne(String key, String path, String name) {
        return fileMetadataRepository.findByKeyAndPathAndName(key, path, name);
    }

    public boolean isFilePresented(StorageKey storageKey) {
        return findOne(storageKey.getKey(), storageKey.getPath(), storageKey.getObjectName()).isPresent();
    }

    public boolean isFilePresented(String key, String path, String name) {
        return findOne(key, path, name).isPresent();
    }

    public List<ResourceInfo> findByKeyAndPath(String key, String path) {
        return fileMetadataRepository.findByKeyAndPath(key, path);
    }

    public List<ResourceInfoDto> findByKeyAndNameContaining(String key, String name) {
        return fileMetadataRepository.findByKeyAndNameContainingIgnoreCase(key, name).stream()
                .map(resourceInfoMapper::toResourceInfoDto).toList();
    }

    @Transactional
    public void save(StorageKey storageKey, Long size) {
        fileMetadataRepository.save(resourceInfoMapper.toResourceInfo(storageKey, size));
    }

    @Transactional
    public void save(StorageKey storageKey) {
        fileMetadataRepository.save(resourceInfoMapper.toResourceInfo(storageKey));
    }

    @Transactional
    public void deleteByPath(String key, String path) {
        fileMetadataRepository.deleteByKeyAndPath(key, path);
    }

    @Transactional
    public void deleteByStorageKey(StorageKey storageKey) {
        //TODO: validate storage key
        fileMetadataRepository.delete(resourceInfoMapper.toResourceInfo(storageKey));
    }
}
