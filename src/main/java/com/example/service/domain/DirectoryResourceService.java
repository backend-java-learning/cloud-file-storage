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
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@AllArgsConstructor
public class DirectoryResourceService implements ResourceService, DirectoryService {

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
        for (String oldPath : results) {
            String newPath = oldPath.replace(sourceStorageKey.buildKey(), targetStorageKey.buildKey());
            StorageKey oldStorageKey = StorageKey.parsePath(oldPath);
            StorageKey newStorageKey = StorageKey.parsePath(newPath);
            storageService.moveObject(oldStorageKey, newStorageKey);
        }
        return getInfo(targetStorageKey);
    }

    @Override
    public List<ResourceInfoResponse> upload(StorageKey storageKey, MultipartFile file) {
        return List.of();
    }

    @Override
    public void createEmptyFolder(StorageKey storageKey) {
        storageService.putEmptyFolder(storageKey);
    }

    @Override
    public List<ResourceInfoResponse> getDirectoryDetails(StorageKey storageKey) {
        List<Item> items = storageService.getListObjectItems(storageKey, false);
        List<ResourceInfoResponse> resourceInfoResponses = new ArrayList<>();
        for (Item item : items) {
            if (item.objectName().equals(storageKey.buildKey())) {
                continue;
            }
            StorageKey storageKeyInfo = StorageKey.parsePath(item.objectName());
            ResourceInfoResponse response = resourceInfoMapper.toResourceInfo(storageKeyInfo, item.size());
            resourceInfoResponses.add(response);
        }
        return resourceInfoResponses;
    }
}
