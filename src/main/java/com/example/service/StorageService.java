package com.example.service;

import com.example.exception.DeleteResourceException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.StorageException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class StorageService {

    @Value("${MINIO_BUCKET}")
    private String bucket;
    private MinioClient minioClient;

    public void putObject(int userId, String relativePath, MultipartFile file) {
        String key = "%s/%s".formatted(addUserPrefix(userId, relativePath), file.getOriginalFilename());
        putObject(key, file, file.getSize(), file.getContentType());
    }

    public void createEmptyFolder(int userId) {
        createEmptyFolder(userId, "");
    }


    public void createEmptyFolder(int userId, String relativePath) {
        String key = addUserPrefix(userId, relativePath);
        InputStreamResource emptyStream = new InputStreamResource(new ByteArrayInputStream(new byte[0]));
        putObject(key, emptyStream, 0, "application/x-directory");
    }

    public void removeObjects(int userId) {
        removeObjects(userId, "");
    }

    public void removeObjects(int userId, String relativePath) {
        String key = addUserPrefix(userId, relativePath);
        var objectsToDelete = getDeleteObjects(key);
        if (objectsToDelete.isEmpty()) {
            throw new ResourceNotFoundException("The directory [%s] doesn't exist".formatted(relativePath));
        }
        var deleteErrors = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objectsToDelete)
                        .build()
        );
        if (StreamSupport.stream(deleteErrors.spliterator(), false).findAny().isPresent()) {
            throw new DeleteResourceException("Unexpected issue while deleting objects by key [%s]".formatted(relativePath));
        }
    }

    public void copyObject(int userId, String targetPath, String sourcePath) {
        String targetKey = addUserPrefix(userId, targetPath);
        String sourceKey = addUserPrefix(userId, sourcePath);
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(targetKey)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucket)
                                            .object(sourceKey)
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

    public void removeObject(int userId, String relativePath) {
        String key = addUserPrefix(userId, relativePath);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object [%s] doesn't exist".formatted(relativePath));
            }
            throw new DeleteResourceException(relativePath, e);
        } catch (Exception e) {
            throw new DeleteResourceException(relativePath, e);
        }
    }

    public StatObjectResponse getStatObject(int userId, String relativePath) {
        String key = addUserPrefix(userId, relativePath);
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object [%s] doesn't exist".formatted(relativePath));
            }
            throw new StorageException("Unexpected issue while getting object information and metadata of an object", errorResponseException);
        } catch (Exception e) {
            throw new StorageException("Unexpected issue while getting object information and metadata of an object", e);
        }
    }

    public GetObjectResponse getObject(int userId, String relativePath) {
        String key = addUserPrefix(userId, relativePath);
        return getObject(key);
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

    public List<Result<Item>> getListObjects(int userId, String prefix) {
        return getListObjects(userId, prefix, false);
    }

    public List<Result<Item>> getListObjects(int userId, String prefix, boolean isRecursive) {
        Iterable<Result<Item>> results = listObjects(userId, prefix, isRecursive);
        return StreamSupport.stream(results.spliterator(), false).toList();
    }

    private void putObject(String key, InputStreamSource inputStreamSource, long objectSize, String contentType) {
        try (InputStream is = inputStreamSource.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
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
                            .formatted(key, bucket), ioException);
        } catch (Exception exception) {
            throw new StorageException("Unexpected issue while put object in bucket", exception);
        }
    }

    public Iterable<Result<Item>> listObjects(int userId, String relativePath, boolean isRecursive) {
        String prefix = addUserPrefix(userId, relativePath);
        return listObjects(prefix, isRecursive);
    }

    public List<String> getObjectsNames(int userId, String relativePath, boolean isRecursive) {
        String prefix = addUserPrefix(userId, relativePath);
        return StreamSupport.stream(listObjects(prefix, isRecursive).spliterator(), false)
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

    private String addUserPrefix(int userId, String objectKey) {
        return "user-%s-files/%s".formatted(userId, objectKey);
    }
}
