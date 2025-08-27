package com.example.service;

import com.example.dto.DownloadResult;
import com.example.exception.StorageException;
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
        return isDirectory(userId, objectKey)
                ? downloadDirectoryAsZip(userId, objectKey)
                : downloadFile(userId, objectKey);
    }

    private DownloadResult downloadFile(int userId, String objectKey) {
        try {
            InputStream stream = storageService.getObject(userId, objectKey);
            Resource resource = new InputStreamResource(stream);
            StatObjectResponse stat = storageService.getStatObject(userId, objectKey);
            return new DownloadResult(
                    Paths.get(objectKey).getFileName().toString(),
                    resource,
                    stat.size(),
                    stat.contentType() != null ? stat.contentType() : "application/octet-stream"
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download object: " + objectKey, e);
        }
    }

    private DownloadResult downloadDirectoryAsZip(int userId, String prefix) throws Exception {
        File tempZip = File.createTempFile("minio-", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempZip);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            Iterable<Result<Item>> results = storageService.getListObjects(userId, prefix.endsWith("/") ? prefix : prefix + "/", true);
            for (Result<Item> result : results) {
                Item item = result.get();
                try (InputStream is = storageService.getObject(item.objectName())) {
                    String entryName = item.objectName().substring(prefix.length());
                    zos.putNextEntry(new ZipEntry(entryName));
                    is.transferTo(zos);
                    zos.closeEntry();
                }
            }
        }
        Resource resource = new FileSystemResource(tempZip);
        return new DownloadResult(
                Paths.get(prefix).getFileName().toString() + ".zip",
                resource,
                tempZip.length(),
                "application/zip"
        );
    }

    private boolean isDirectory(int userId, String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        Iterable<Result<Item>> results = storageService.listObjects(userId, path, false);
        return results.iterator().hasNext();
    }
}
