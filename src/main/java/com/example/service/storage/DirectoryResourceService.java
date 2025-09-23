package com.example.service.storage;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoDto;
import com.example.dto.enums.ResourceType;
import com.example.exception.StorageException;
import com.example.exception.resource.ResourceAlreadyExist;
import com.example.exception.resource.ResourceException;
import com.example.exception.resource.ResourceNotFoundException;
import com.example.exception.resource.ResourceTypeException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.DirectoryService;
import com.example.service.minio.StorageService;
import com.example.utils.ZipCreator;
import io.minio.Result;
import io.minio.messages.Item;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class DirectoryResourceService extends AbstractResourceService implements DirectoryService {

    private final ZipCreator zipCreator;

    public DirectoryResourceService(StorageService storageService,
                                    ResourceInfoMapper resourceInfoMapper,
                                    ZipCreator zipCreator) {
        super(storageService, resourceInfoMapper);
        this.zipCreator = zipCreator;
    }

    @Override
    public ResourceInfoDto getInfo(StorageKey storageKey) {
        validateDirectory(storageKey);
        if (!storageService.doesObjectExist(storageKey)) {
            log.error("Directory [{}] doesn't exist", storageKey.buildKey());
            throw new ResourceNotFoundException("Directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        return resourceInfoMapper.toResourceInfoDto(storageKey);
    }

    @Override
    public void remove(StorageKey storageKey) {
        validateDirectory(storageKey);
        storageService.removeObjects(storageKey);
    }

    @Override
    public DownloadResult downloadStream(StorageKey storageKey) {
        validateDirectory(storageKey);
        List<ZipCreator.FileEntry> files = storageService.getObjectsNames(storageKey, true).stream()
                .map(objectName -> {
                    String base = storageKey.buildKey();
                    String entryName = objectName.substring(base.length());
                    return new ZipCreator.FileEntry(entryName, () -> storageService.getObject(objectName));
                })
                .collect(Collectors.toList());
        return new DownloadResult(
                storageKey.getObjectName().replace("/", "") + ".zip",
                zipCreator.createZip(files),
                "application/zip"
        );
    }

    @Override
    public ResourceInfoDto createEmptyFolder(StorageKey storageKey) {
        validateDirectory(storageKey);
        if (storageService.doesObjectExist(storageKey)) {
            throw new ResourceException("Move file exception: the file [%s] already exist".formatted(storageKey.getPath()));
        }
        storageService.putEmptyFolder(storageKey);
        return getInfo(storageKey);
    }

    @Override
    public List<ResourceInfoDto> getDirectoryDetails(StorageKey storageKey) {
        validateDirectory(storageKey);
        if (!storageService.doesObjectExist(storageKey)) {
            throw new ResourceNotFoundException("Get directory details exception: the directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        return StreamSupport.stream(storageService.listObjects(storageKey, false).spliterator(), false)
                .map(result -> process(result))
                .filter(resourceInfoDto -> !(resourceInfoDto.getPath().equals(storageKey.getPrefix())
                        && resourceInfoDto.getName().equals(storageKey.getObjectName())))
                .toList();
    }

    private ResourceInfoDto process(Result<Item> result) {
        try {
            Item item = result.get();
            String objectName = item.objectName();
            StorageKey storageKeyInfo = StorageKey.parsePath(objectName);
            return storageKeyInfo.getResourceType().equals(ResourceType.FILE)
                    ? resourceInfoMapper.toResourceInfoDto(storageKeyInfo, item.size())
                    : resourceInfoMapper.toResourceInfoDto(storageKeyInfo);
        } catch (Exception ex) {
            log.error("Exception in STREAM");
            throw new StorageException("Unexpected issue");
        }
    }

    @Override
    public ResourceInfoDto move(StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        if (sourceStorageKey.getResourceType() != targetStorageKey.getResourceType()) {
            throw new ResourceTypeException("Source and target key must have the same resource type [FILE | DIRECTORY]");
        }
        validateDirectory(sourceStorageKey);
        if (storageService.doesObjectExist(targetStorageKey)) {
            throw new ResourceAlreadyExist("Move file exception: the file [%s] already exist".formatted(targetStorageKey.getPath()));
        }
        storageService.getObjectsNames(sourceStorageKey, true)
                .forEach(oldPath -> moveObject(oldPath, sourceStorageKey, targetStorageKey));
        return getInfo(targetStorageKey);
    }

    private void moveObject(String oldPath, StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        String newPath = oldPath.replace(sourceStorageKey.buildKey(), targetStorageKey.buildKey());
        StorageKey oldStorageKey = StorageKey.parsePath(oldPath);
        StorageKey newStorageKey = StorageKey.parsePath(newPath);
        storageService.moveObject(oldStorageKey, newStorageKey);
    }

    private void validateDirectory(StorageKey storageKey) {
        validateResource(storageKey, ResourceType.DIRECTORY);
    }
}
