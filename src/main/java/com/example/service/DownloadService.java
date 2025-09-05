package com.example.service;

import com.example.dto.DownloadResult;
import com.example.exception.StorageException;
import com.example.models.StorageKey;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@AllArgsConstructor
public class DownloadService {

    private StorageService storageService;

    public DownloadResult download(int userId, String objectKey) throws Exception {
        StorageKey storageKey = new StorageKey(userId, objectKey);
        return isDirectory(storageKey)
                ? downloadDirectoryAsZip(storageKey)
                : downloadFile(storageKey);
    }

    private DownloadResult downloadFile(StorageKey storageKey) {
        try {
            InputStream stream = storageService.getObject(storageKey);
            Resource resource = new InputStreamResource(stream);
            StatObjectResponse stat = storageService.getStatObject(storageKey);
            return new DownloadResult(
                    Paths.get(storageKey.relativePath()).getFileName().toString(),
                    resource,
                    stat.size(),
                    stat.contentType() != null ? stat.contentType() : "application/octet-stream"
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download object: " + storageKey.buildKey(), e);
        }
    }

    private DownloadResult downloadDirectoryAsZip(StorageKey storageKey) throws Exception {
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
    }

    private boolean isDirectory(StorageKey storageKey) {
//        if (!path.endsWith("/")) {
//            path = path + "/";
//        }
        return storageService.doesObjectExist(storageKey);
    }
}
