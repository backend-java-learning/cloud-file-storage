package com.example.service.minio;

import com.example.dto.ResourceInfoDto;
import com.example.dto.enums.ResourceType;
import com.example.exception.resource.DeleteResourceException;
import com.example.exception.resource.ResourceNotFoundException;
import com.example.exception.StorageException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final String bucket;
    private final MinioClient minioClient;
    private final MinioArgsFactory minioArgsFactory;
    private final ResourceInfoMapper resourceInfoMapper;

    public void putObject(StorageKey storageKey, MultipartFile file) {
        putObject(storageKey, file, file.getSize(), file.getContentType());
    }

    public void putEmptyFolder(StorageKey storageKey) {
        InputStreamResource emptyStream = new InputStreamResource(new ByteArrayInputStream(new byte[0]));
        putObject(storageKey, emptyStream, 0, "application/x-directory");
    }

    public void removeObjects(int userId) {
        removeObjects(StorageKey.createEmptyDirectoryKey(userId));
    }

    public boolean doesObjectExist(StorageKey storageKey) {
        return listObjects(storageKey.buildKey(), false).iterator().hasNext();
    }

    public void removeObjects(StorageKey storageKey) {
        var objectsToDelete = getDeleteObjects(storageKey.buildKey());
        if (objectsToDelete.isEmpty()) {
            throw new ResourceNotFoundException("The directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        var deleteErrors = minioClient.removeObjects(minioArgsFactory.removeObjectsArgs(objectsToDelete));
        if (StreamSupport.stream(deleteErrors.spliterator(), false).findAny().isPresent()) {
            throw new DeleteResourceException("Unexpected issue while deleting objects by key [%s]".formatted(storageKey.getPath()));
        }
    }

    public void copyObject(StorageKey targetKey, StorageKey sourceKey) {
        try {
            minioClient.copyObject(minioArgsFactory.copyObjectArgs(sourceKey, targetKey));
        } catch (Exception e) {
            handleMinioError(e, "", "Unexpected issue while copying object");
        }
    }

    public void removeObject(StorageKey storageKey) {
        try {
            minioClient.removeObject(minioArgsFactory.removeObjectArgs(storageKey));
        } catch (Exception e) {
            handleMinioError(e, storageKey.getPath(), "Deletion error: The object [%s] could not be removed from storage due to an unexpected issue.".formatted(storageKey.getPath()));
        }
    }

    public StatObjectResponse getStatObject(StorageKey storageKey) {
        try {
            return minioClient.statObject(minioArgsFactory.statObjectArgs(storageKey));
        } catch (Exception e) {
            handleMinioError(e, "", "Unexpected issue while getting object information and metadata of an object");
            return null;
        }
    }

    public GetObjectResponse getObject(StorageKey storageKey) {
        return getObject(storageKey.buildKey());
    }

    public GetObjectResponse getObject(String objectName) {
        try {
            return minioClient.getObject(minioArgsFactory.getObjectArgs(objectName));
        } catch (Exception e) {
            handleMinioError(e, "", "Unexpected issue while getting object");
            return null;
        }
    }

    public List<String> getObjectsNames(StorageKey storageKey, boolean isRecursive) {
        return StreamSupport.stream(listObjects(storageKey.buildKey(), isRecursive).spliterator(), false)
                .map(result -> {
                    try {
                        return result.get().objectName();
                    } catch (Exception ex) {
                        throw new StorageException("Unexpected issue");
                    }
                }).toList();
    }

    public List<ResourceInfoDto> getObjectsInfo(StorageKey storageKey, boolean isRecursive) {
        return StreamSupport.stream(listObjects(storageKey.buildKey(), isRecursive).spliterator(), false)
                .map(result -> {
                    try {
                        Item item = result.get();
                        String objectName = item.objectName();
                        StorageKey storageKeyInfo = StorageKey.parsePath(objectName);
                        return storageKeyInfo.getResourceType().equals(ResourceType.FILE)
                                ? resourceInfoMapper.toResourceInfoDto(storageKeyInfo, item.size())
                                : resourceInfoMapper.toResourceInfoDto(storageKeyInfo);
                    } catch (Exception ex) {
                        throw new StorageException("Unexpected issue");
                    }
                }).toList();
    }

    public void moveObject(StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        copyObject(targetStorageKey, sourceStorageKey);
        removeObject(sourceStorageKey);
    }

    public Iterable<Result<Item>> listObjects(StorageKey storageKey, boolean isRecursive) {
        return listObjects(storageKey.buildKey(), isRecursive);
    }

    public Iterable<Result<Item>> listObjects(String prefix, boolean isRecursive) {
        return minioClient.listObjects(minioArgsFactory.listObjectsArgs(prefix, isRecursive));
    }

    private List<DeleteObject> getDeleteObjects(String prefix) {
        Iterable<Result<Item>> results = listObjects(prefix, true);
        List<DeleteObject> deleteObjects = new ArrayList<>();
        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                deleteObjects.add(new DeleteObject(item.objectName()));
            } catch (Exception e) {
                log.error("Failed to process object for deletion in bucket [{}] with prefix [{}]", bucket, prefix, e);
                throw new StorageException("Error while building delete objects list for bucket [%s], prefix [%s]"
                        .formatted(bucket, prefix), e);
            }
        }
        return deleteObjects;
    }

    private ObjectWriteResponse putObject(StorageKey storageKey, InputStreamSource inputStreamSource, long objectSize, String contentType) {
        try (InputStream is = inputStreamSource.getInputStream()) {
            return minioClient.putObject(minioArgsFactory.putObjectArgs(storageKey, is, objectSize, contentType));
        } catch (Exception e) {
            handleMinioError(e, "", "Unexpected issue while put object in bucket");
            return null;
        }
    }

    private void handleMinioError(Exception e, String path, String message) {
        if (e instanceof ErrorResponseException errorResponseException) {
            if ("NoSuchKey".equals(errorResponseException.errorResponse().code())) {
                throw new ResourceNotFoundException("Object [%s] doesn't exist".formatted(path));
            }
            throw new StorageException("MinIO error for object [%s]: %s".formatted(path, errorResponseException.errorResponse().message()), e);
        }
        throw new StorageException(message, e);
    }
}
