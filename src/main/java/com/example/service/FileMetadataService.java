package com.example.service;

import com.example.dto.ResourceInfoDto;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.FileMetadataMapper;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.FileMetadata;
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
    private final FileMetadataMapper fileMetadataMapper;
    private final ResourceInfoMapper resourceInfoMapper;

    @Transactional
    public void updateFileMetadata(StorageKey oldKey, StorageKey newKey) {
        FileMetadata fileMetadata = fileMetadataRepository.findByKeyAndPathAndName(oldKey.getKey(), oldKey.getPrefix(),
                        oldKey.getObjectName())
                .orElseThrow(() -> new ResourceNotFoundException("Object doesn't exist"));
        fileMetadata.setKey(newKey.getKey());
        fileMetadata.setPath(newKey.getPrefix());
        fileMetadata.setName(newKey.getObjectName());
        fileMetadata.setType(newKey.getResourceType());
    }

    public Optional<FileMetadata> findByStorageKey(StorageKey storageKey) {
        FileMetadata fileMetadata = fileMetadataMapper.of(storageKey);
        //TODO: add validation
        return fileMetadataRepository.findOne(Example.of(fileMetadata));
    }

    public Optional<FileMetadata> findOne(String key, String path, String name) {
        return fileMetadataRepository.findByKeyAndPathAndName(key, path, name);
    }

    public List<FileMetadata> findByKeyAndPath(String key, String path) {
        return fileMetadataRepository.findByKeyAndPath(key, path);
    }

    public List<ResourceInfoDto> findByKeyAndNameContaining(String key, String name) {
        return fileMetadataRepository.findByKeyAndNameContainingIgnoreCase(key, name).stream()
                .map(resourceInfoMapper::toResourceInfoDto).toList();
    }

    @Transactional
    public void save(StorageKey storageKey, Long size) {
        fileMetadataRepository.save(fileMetadataMapper.of(storageKey, size));
    }

    @Transactional
    public void save(StorageKey storageKey) {
        fileMetadataRepository.save(fileMetadataMapper.of(storageKey));
    }

    @Transactional
    public void deleteByPath(String key, String path) {
        fileMetadataRepository.deleteByKeyAndPath(key, path);
    }

    @Transactional
    public void deleteByStorageKey(StorageKey storageKey) {
        //TODO: validate storage key
        fileMetadataRepository.delete(fileMetadataMapper.of(storageKey));
    }
}
