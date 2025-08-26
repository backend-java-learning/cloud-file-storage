package com.example.service;

import com.example.mapper.ResourceInfoMapper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@AllArgsConstructor
public class MinioService {

    private MinioClient minioClient;

    public ObjectWriteResponse createFolder(String folderName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(folderName)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)// имя заканчивается на "/"
                        .build()
        );
    }

    public void removeFolder(String folder) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> results = getFolderFiles(folder);
        removeObjects(results);
    }

    private Iterable<Result<Item>> getFolderFiles(String folder) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user-files")
                        .prefix(folder)   // удаляем всё, что начинается с этого префикса
                        .recursive(true)  // рекурсивно
                        .build()
        );
    }

    //TODO: handle exceptions
    private void removeObjects(Iterable<Result<Item>> results) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (Result<Item> result : results) {
            String objectName = result.get().objectName();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
                            .object(objectName)
                            .build()
            );
        }
    }

    public void deleteBucketIfExist(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        )) {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    public List<String> getListOfBuckets() {
        try {
            return minioClient.listBuckets()
                    .stream().map(
                            bucket -> bucket.name()
                    ).toList();
        } catch (Exception ex) {

            return List.of();
            // throw new InvalidClassException("exception");
        }
    }

}
