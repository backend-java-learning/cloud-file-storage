package com.example.service.domain;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.ResourcePath;
import com.example.models.StorageKey;
import com.example.service.StorageService;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Paths;

@Service
@AllArgsConstructor
public class FileService implements ResourceService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        String fileNameWithPath = statObjectResponse.object().replace(storageKey.getPrefix(), "");
        ResourcePath resourcePath = ResourcePath.of(fileNameWithPath);
        return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), resourcePath.getParentPath(),
                resourcePath.getFolderName(), ResourceType.FILE);
    }

    @Override
    public void remove(StorageKey storageKey) {
        storageService.removeObject(storageKey);
    }

    @Override
    public DownloadResult download(StorageKey storageKey) {
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

    @Override
    public ResourceInfoResponse rename() {
        return null;
    }
}
