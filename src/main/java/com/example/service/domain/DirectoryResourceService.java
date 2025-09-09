package com.example.service.domain;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.StorageService;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@AllArgsConstructor
public class DirectoryResourceService implements ResourceService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        if (!storageService.doesObjectExist(storageKey)) {
            log.error("Directory [{}] doesn't exist", storageKey.buildKey());
            throw new ResourceNotFoundException("Directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        return resourceInfoMapper.toResourceInfo(storageKey);
    }

    @Override
    public void remove(StorageKey storageKey) {
        storageService.removeObjects(storageKey);
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
    public ResourceInfoResponse move(StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        List<String> results = storageService.getObjectsNames(sourceStorageKey, true);
        for (String sourceKey : results) {
            StorageKey tempSourceStorageKey = StorageKey.parsePath(sourceKey);
            StorageKey tempTargetStorageKey = tempSourceStorageKey.updatePrefix(targetStorageKey.getPrefix());
            storageService.moveObject(tempSourceStorageKey, tempTargetStorageKey);
        }
        return getInfo(targetStorageKey);
    }

    @Override
    public List<ResourceInfoResponse> upload(StorageKey storageKey) {
        return List.of();
    }

    public void createEmptyFolder(StorageKey storageKey) {
        storageService.putEmptyFolder(storageKey);
    }

    public List<ResourceInfoResponse> getDirectoryDetails(StorageKey storageKey) {
        //List<Item> items = storageService.getListObjectItems(storageKey, false);
        //resourceInfoMapper.
        return List.of();
    }
}
