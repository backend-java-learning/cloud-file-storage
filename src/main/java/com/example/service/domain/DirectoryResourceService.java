package com.example.service.domain;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.FileMetadata;
import com.example.models.StorageKey;
import com.example.service.DirectoryService;
import com.example.service.FileMetadataService;
import com.example.service.StorageService;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class DirectoryResourceService extends AbstractResourceService implements DirectoryService {

    public DirectoryResourceService(StorageService storageService,
                                    FileMetadataService fileMetadataService,
                                    ResourceInfoMapper resourceInfoMapper) {
        super(storageService, fileMetadataService, resourceInfoMapper);
    }

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        //TODO: Validate
        return super.getInfo(storageKey);
    }

    @Override
    public void remove(StorageKey storageKey) {
        storageService.removeObjects(storageKey);
        fileMetadataService.deleteByPath(storageKey.getKey(), storageKey.getPrefix());
    }

    @Override
    public DownloadResult download(StorageKey storageKey) {
        try {
            File tempZip = File.createTempFile("minio-", ".zip");
            try (FileOutputStream fos = new FileOutputStream(tempZip);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                Iterable<Result<Item>> results = storageService.getListObjects(storageKey, true);
                for (Result<Item> result : results) {
                    Item item = result.get();
                    try (InputStream is = storageService.getObject(item.objectName())) {
                        String entryName = item.objectName().substring(storageKey.buildKey().length());
                        zos.putNextEntry(new ZipEntry(entryName));
                        is.transferTo(zos);
                        zos.closeEntry();
                    }
                }
            }
            Resource resource = new FileSystemResource(tempZip);
            return new DownloadResult(
                    Paths.get(storageKey.buildKey()).getFileName().toString() + ".zip",
                    resource,
                    tempZip.length(),
                    "application/zip"
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download object: " + storageKey.buildKey(), e);
        }

    }

    @Override
    public ResourceInfoResponse createEmptyFolder(StorageKey storageKey) {
        storageService.putEmptyFolder(storageKey);
        fileMetadataService.save(storageKey);
        return getInfo(storageKey);
    }

    @Override
    public List<ResourceInfoResponse> getDirectoryDetails(StorageKey storageKey) {
        Optional<FileMetadata> fileMetadataOptional = fileMetadataService.findOne(storageKey.getKey(), storageKey.getPrefix(),
                storageKey.getObjectName());
        if (fileMetadataOptional.isEmpty()) {
            throw new ResourceNotFoundException("Directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        List<FileMetadata> filesMetadata = fileMetadataService.findByKeyAndPath(storageKey.getKey(), storageKey.getPath());
        return filesMetadata.stream()
                .filter(fileMetadata -> !fileMetadata.getName().equals(storageKey.getObjectName()))
                .map(resourceInfoMapper::toDto).toList();
    }

    @Override
    public ResourceInfoResponse move(StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        //TODO: check different types
        List<String> results = storageService.getObjectsNames(sourceStorageKey, true);
        for (String oldPath : results) {
            String newPath = oldPath.replace(sourceStorageKey.buildKey(), targetStorageKey.buildKey());
            StorageKey oldStorageKey = StorageKey.parsePath(oldPath);
            StorageKey newStorageKey = StorageKey.parsePath(newPath);
            storageService.moveObject(oldStorageKey, newStorageKey);
            fileMetadataService.updateFileMetadata(oldStorageKey, newStorageKey);
        }
        return getInfo(targetStorageKey);
    }
}
