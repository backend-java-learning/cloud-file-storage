package com.example.service.storage;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoDto;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.FileMetadataService;
import com.example.service.StorageService;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@Service
public class FileResourceService extends AbstractResourceService {

    public FileResourceService(StorageService storageService,
                               FileMetadataService fileMetadataService,
                               ResourceInfoMapper resourceInfoMapper) {
        super(storageService, fileMetadataService, resourceInfoMapper);
    }

    @Override
    public ResourceInfoDto getInfo(StorageKey storageKey) {
        //TODO: Validation
        return super.getInfo(storageKey);
    }

    @Override
    public ResourceInfoDto move(StorageKey sourcePrefix, StorageKey targetPrefix) {
        storageService.moveObject(sourcePrefix, targetPrefix);
        fileMetadataService.updateFileMetadata(sourcePrefix, targetPrefix);
        return getInfo(targetPrefix);
    }

    @Override
    public DownloadResult downloadStream(StorageKey storageKey) {
        StreamingResponseBody body = out -> {
            try (InputStream is = storageService.getObject(storageKey)) {
                is.transferTo(out);
            }
        };
        return new DownloadResult(storageKey.getPath(), body,"application/octet-stream");
    }

    @Override
    public void remove(StorageKey storageKey) {
        storageService.removeObject(storageKey);
        fileMetadataService.deleteByStorageKey(storageKey);
    }
}
