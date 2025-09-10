package com.example.service.domain;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.StorageService;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@AllArgsConstructor
public class FileResourceService implements ResourceService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        StatObjectResponse statObjectResponse = storageService.getStatObject(storageKey);
        StorageKey statObjectStorageKey = StorageKey.parsePath(statObjectResponse.object());
        return resourceInfoMapper.toResourceInfo(statObjectStorageKey, statObjectResponse.size());
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
                    storageKey.getPath(),
                    resource,
                    stat.size(),
                    stat.contentType() != null ? stat.contentType() : "application/octet-stream"
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download object: " + storageKey.buildKey(), e);
        }
    }

    @Override
    public ResourceInfoResponse move(StorageKey sourcePrefix, StorageKey targetPrefix) {
        storageService.moveObject(sourcePrefix, targetPrefix);
        return getInfo(targetPrefix);
    }

    @Override
    public List<ResourceInfoResponse> upload(StorageKey storageKey, MultipartFile file) {
        storageService.putObject(storageKey, file);
        return List.of(getInfo(storageKey));
    }
}
