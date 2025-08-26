package com.example.service;

import com.example.exception.ResourceNotFoundException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class StorageService {
//    getObject(bucket, key)
//
//    deleteObject(bucket, key)
//
//    listObjects(bucket, prefix, recursive)
//
//    copyObject(bucket, fromKey, toKey)

    private MinioClient minioClient;

    public void putObject(String bucket, String objectName, MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }
    }

    public StatObjectResponse getStatObject(String bucket, String key) throws ServerException, InsufficientDataException,
            ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
            XmlParserException, InternalException {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
        } catch (ErrorResponseException errorResponseException) {
            if (errorResponseException.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceNotFoundException("Object doesn't exist");
            }
            throw errorResponseException;
        }
    }

    public List<Result<Item>> getListObjects(String bucket, String prefix) {
        return getListObjects(bucket, prefix, false);
    }

    public List<Result<Item>> getListObjects(String bucket, String prefix, boolean isRecursive) {
        Iterable<Result<Item>> results =
                minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucket)
                                .prefix(prefix)
                                .recursive(isRecursive)
                                .build()
                );
        return StreamSupport.stream(results.spliterator(), false).toList();
    }
}
