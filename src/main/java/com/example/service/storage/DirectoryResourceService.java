package com.example.service.storage;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoDto;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.ResourceInfo;
import com.example.models.StorageKey;
import com.example.service.DirectoryService;
import com.example.service.FileMetadataService;
import com.example.service.StorageService;
import com.example.utils.ZipCreator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DirectoryResourceService extends AbstractResourceService implements DirectoryService {

    private final ZipCreator zipCreator;

    public DirectoryResourceService(StorageService storageService,
                                    FileMetadataService fileMetadataService,
                                    ResourceInfoMapper resourceInfoMapper,
                                    ZipCreator zipCreator) {
        super(storageService, fileMetadataService, resourceInfoMapper);
        this.zipCreator = zipCreator;
    }

    @Override
    public ResourceInfoDto getInfo(StorageKey storageKey) {
        //TODO: Validate
        return super.getInfo(storageKey);
    }

    @Override
    public void remove(StorageKey storageKey) {
        storageService.removeObjects(storageKey);
        fileMetadataService.deleteByPath(storageKey.getKey(), storageKey.getPrefix());
    }

    @Override
    public DownloadResult downloadStream(StorageKey storageKey) {
        List<ZipCreator.FileEntry> files = storageService.getObjectsNames(storageKey, true).stream()
                .map(objectName -> {
                    InputStream is = storageService.getObject(objectName);
                    String base = storageKey.buildKey();
                    String entryName = objectName.substring(base.length());
                    return new ZipCreator.FileEntry(entryName, is);
                })
                .collect(Collectors.toList());
        return new DownloadResult(
                Paths.get(storageKey.buildKey()).getFileName().toString() + ".zip",
                zipCreator.createZip(files),
                "application/zip"
        );
    }

    @Override
    public ResourceInfoDto createEmptyFolder(StorageKey storageKey) {
        storageService.putEmptyFolder(storageKey);
        fileMetadataService.save(storageKey);
        return getInfo(storageKey);
    }

    @Override
    public List<ResourceInfoDto> getDirectoryDetails(StorageKey storageKey) {
        Optional<ResourceInfo> fileMetadataOptional = fileMetadataService.findOne(storageKey.getKey(), storageKey.getPrefix(),
                storageKey.getObjectName());
        if (fileMetadataOptional.isEmpty()) {
            throw new ResourceNotFoundException("Directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        List<ResourceInfo> filesMetadata = fileMetadataService.findByKeyAndPath(storageKey.getKey(), storageKey.getPath());
        return filesMetadata.stream()
                .filter(fileMetadata -> !fileMetadata.getName().equals(storageKey.getObjectName()))
                .map(resourceInfoMapper::toResourceInfoDto).toList();
    }

    @Override
    public ResourceInfoDto move(StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        //TODO: check different types
        storageService.getObjectsNames(sourceStorageKey, true)
                .forEach(oldPath -> moveObject(oldPath, sourceStorageKey, targetStorageKey));
        return getInfo(targetStorageKey);
    }

    @Transactional(rollbackOn = Exception.class)
    private void moveObject(String oldPath, StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        String newPath = oldPath.replace(sourceStorageKey.buildKey(), targetStorageKey.buildKey());
        StorageKey oldStorageKey = StorageKey.parsePath(oldPath);
        StorageKey newStorageKey = StorageKey.parsePath(newPath);
        fileMetadataService.updateFileMetadata(oldStorageKey, newStorageKey);
        storageService.moveObject(oldStorageKey, newStorageKey);
    }
}
