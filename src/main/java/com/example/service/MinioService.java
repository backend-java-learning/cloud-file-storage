package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
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
    private ResourceInfoMapper resourceInfoMapper;

    public StatObjectResponse getObject(String name) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket("my-bucket")
                            .object(name)
                            .build()
            );
        } catch (Exception e) {
            return null;
        }
    }

    public ResourceInfoResponse getResourceInfo(String resourceName) {
        return resourceName.endsWith("/")
                ? getDirectoryInfo(resourceName)
                : getFileInfo(resourceName);
    }

    private ResourceInfoResponse getDirectoryInfo(String folderName) {
        return null;
    }

    private ResourceInfoResponse getFileInfo(String fileName) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket("my-bucket")
                            .object(fileName)
                            .build()
            );
            String fileNameWithPath = statObjectResponse.object();
            int lastIndexOfSplitter = fileNameWithPath.lastIndexOf("/");
            String folderName = fileNameWithPath.substring(0, lastIndexOfSplitter);
            String fileName1 = fileNameWithPath.substring(lastIndexOfSplitter + 1, fileNameWithPath.length() - 1);
            return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), folderName, fileName1, ResourceType.FILE);
        } catch (Exception e) {
            return null;
        }
    }


    public void createBucket(String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(name)
                        .build()
        );
    }

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
