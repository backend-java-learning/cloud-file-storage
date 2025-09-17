package com.example.service;

import com.example.exception.DeleteResourceException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.StorageException;
import com.example.models.StorageKey;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${MINIO_BUCKET}")
    private String bucket;
    private final MinioClient minioClient;

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

    public void removeObjects(StorageKey storageKey) {
        var objectsToDelete = getDeleteObjects(storageKey.buildKey());
        if (objectsToDelete.isEmpty()) {
            throw new ResourceNotFoundException("The directory [%s] doesn't exist".formatted(storageKey.getPath()));
        }
        var deleteErrors = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objectsToDelete)
                        .build()
        );
        if (StreamSupport.stream(deleteErrors.spliterator(), false).findAny().isPresent()) {
            throw new DeleteResourceException("Unexpected issue while deleting objects by key [%s]".formatted(storageKey.getPath()));
        }
    }

    public void copyObject(StorageKey targetKey, StorageKey sourceKey) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetKey.buildKey())
                            .source(
                                    CopySource.builder()
                                            .bucket(bucket)
                                            .object(sourceKey.buildKey())
                                            .build()
                            )
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object doesn't exist");
            }
            throw new StorageException("Unexpected issue while getting object information and metadata of an object", errorResponseException);
        } catch (Exception e) {
            throw new StorageException("Unexpected issue while copying object", e);
        }
    }

    public void removeObject(StorageKey storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(storageKey.buildKey())
                            .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object [%s] doesn't exist".formatted(storageKey.getPath()));
            }
            throw new DeleteResourceException(storageKey.getPath(), e);
        } catch (Exception e) {
            throw new DeleteResourceException(storageKey.getPath(), e);
        }
    }

    public void moveObject(StorageKey sourceStorageKey, StorageKey targetStorageKey) {
        copyObject(targetStorageKey, sourceStorageKey);
        removeObject(sourceStorageKey);
    }

    public StatObjectResponse getStatObject(StorageKey storageKey) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(storageKey.buildKey())
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object [%s] doesn't exist".formatted(storageKey.getPath()));
            }
            throw new StorageException("Unexpected issue while getting object information and metadata of an object", errorResponseException);
        } catch (Exception e) {
            throw new StorageException("Unexpected issue while getting object information and metadata of an object", e);
        }
    }

    public GetObjectResponse getObject(StorageKey storageKey) {
        return getObject(storageKey.buildKey());
    }

    public GetObjectResponse getObject(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object doesn't exist");
            }
            throw new StorageException("Unexpected issue while getting object", errorResponseException);
        } catch (Exception e) {
            throw new StorageException("Unexpected issue while getting object", e);
        }
    }

//    public List<Result<Item>> getListObjects(StorageKey storageKey, boolean isRecursive) {
//        Iterable<Result<Item>> results = listObjects(storageKey.buildKey(), isRecursive);
//        return StreamSupport.stream(results.spliterator(), false).toList();
//    }

    private ObjectWriteResponse putObject(StorageKey storageKey, InputStreamSource inputStreamSource, long objectSize, String contentType) {
        try (InputStream is = inputStreamSource.getInputStream()) {
            return minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(storageKey.buildKey())
                            .stream(is, objectSize, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object doesn't exist");
            }
            throw new StorageException("Unexpected issue while put object in bucket", errorResponseException);
        } catch (IOException ioException) {
            throw new StorageException(
                    "I/O error while uploading object [%s] to bucket [%s]. Possible network or file stream issue."
                            .formatted(storageKey.buildKey(), bucket), ioException);
        } catch (Exception exception) {
            throw new StorageException("Unexpected issue while put object in bucket", exception);
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

    private Iterable<Result<Item>> listObjects(String prefix, boolean isRecursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(isRecursive)
                        .build()
        );
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
}
