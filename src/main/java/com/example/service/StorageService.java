package com.example.service;

import com.example.exception.DeleteResourceException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.StorageException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@AllArgsConstructor
public class StorageService {
//    getObject(bucket, key)
//
//    copyObject(bucket, fromKey, toKey)

    private MinioClient minioClient;

    public void putObject(int userId, String bucket, String relativePath, MultipartFile file) {
        String key = addUserPrefix(userId, relativePath);
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object [%s] doesn't exist".formatted(relativePath));
            }
            throw new StorageException("Unexpected issue while put object in bucket", errorResponseException);
        } catch (IOException ioException) {
            throw new StorageException(
                    "I/O error while uploading object [%s] to bucket [%s]. Possible network or file stream issue."
                            .formatted(relativePath, bucket), ioException);
        } catch (Exception exception) {
            throw new StorageException("Unexpected issue while put object in bucket", exception);
        }
    }

    public void removeObjects(int userId, String bucket, String relativePath) {
        String key = addUserPrefix(userId, relativePath);
        var objectsToDelete = getDeleteObjects(bucket, key);
        var deleteErrors = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucket)
                        .objects(objectsToDelete)
                        .build()
        );
        List<String> failed = new ArrayList<>();
        for (Result<DeleteError> result : deleteErrors) {
            try {
                DeleteError error = result.get();
                failed.add(stripPrefix(error.objectName()));
            } catch (Exception e) {
                throw new StorageException("Unexpected issue while processing delete results", e);
            }
        }
        if (!failed.isEmpty()) {
            throw new DeleteResourceException(failed);
        }
    }

    public void removeObject(int userId, String bucket, String relativePath) {
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

    public StatObjectResponse getStatObject(int userId, String bucket, String relativePath) {
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

    public List<Result<Item>> getListObjects(int userId, String bucket, String prefix) {
        return getListObjects(userId, bucket, prefix, false);
    }

    public List<Result<Item>> getListObjects(int userId, String bucket, String prefix, boolean isRecursive) {
        Iterable<Result<Item>> results = listObjects(userId, bucket, prefix, isRecursive);
        return StreamSupport.stream(results.spliterator(), false).toList();
    }

    private Iterable<Result<Item>> listObjects(int userId, String bucket, String relativePath, boolean isRecursive) {
        String prefix = addUserPrefix(userId, relativePath);
        return listObjects(bucket, prefix, isRecursive);
    }

    private Iterable<Result<Item>> listObjects(String bucket, String prefix, boolean isRecursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(isRecursive)
                        .build()
        );
    }

    private Iterable<DeleteObject> getDeleteObjects(int userId, String bucket, String prefix) {
        Iterable<Result<Item>> results = listObjects(userId, bucket, prefix, true);
        var streamSupplier = StreamSupport.stream(results.spliterator(), false)
                .map(result -> {
                    try {
                        Item resultItem = result.get();
                        return new DeleteObject(resultItem.objectName());
                    } catch (Exception ex) {
                        //TODO: think about it
                        log.error("");
                        return null;
                    }
                });
        return streamSupplier::iterator;
    }

    private Iterable<DeleteObject> getDeleteObjects(String bucket, String prefix) {
        Iterable<Result<Item>> results = listObjects(bucket, prefix, true);
        var streamSupplier = StreamSupport.stream(results.spliterator(), false)
                .map(result -> {
                    try {
                        Item resultItem = result.get();
                        return new DeleteObject(resultItem.objectName());
                    } catch (Exception ex) {
                        //TODO: think about it
                        log.error("");
                        return null;
                    }
                });
        return streamSupplier::iterator;
    }

    private String stripPrefix(String objectKey) {
        int idx = objectKey.indexOf("-files/");
        return (idx != -1) ? objectKey.substring(idx + "-files/".length()) : objectKey;
    }

    private String addUserPrefix(int userId, String objectKey) {
        return "user-%s-files/%s".formatted(userId, objectKey);
    }
}
