package com.example.service;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.mapper.ResourceInfoMapper;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class ResourceInfoService {

    private MinioClient minioClient;
    private ResourceInfoMapper resourceInfoMapper;

    public ResourceInfoResponse getResourceInfo(String resourceName) {
        return resourceName.endsWith("/")
                ? getDirectoryInfo(resourceName)
                : getFileInfo(resourceName);
    }

    private ResourceInfoResponse getDirectoryInfo(String folderName) {
        Iterable<Result<Item>> results =
                minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket("my-bucket")
                                .prefix(folderName)
                                .build()
                );
        long size = StreamSupport.stream(results.spliterator(), false).count();
        if (size == 0) {
            return null;
        }

        String trimmed = folderName.endsWith("/")
                ? folderName.substring(0, folderName.length() - 1)
                : folderName;
        int lastSlash = trimmed.lastIndexOf("/");

        String path;
        String name;

        if (lastSlash == -1) {
            // Если слешей нет, значит путь пустой, а всё остальное — имя
            path = "";
            name = folderName;
        } else {
            path = folderName.substring(0, lastSlash + 1); // включая последний "/"
            name = folderName.substring(lastSlash + 1);    // имя без "/"

            // Добавляем "/" к имени, если в исходном пути он был
            if (folderName.endsWith("/")) {
                name = name + "/";
            }
        }

        return resourceInfoMapper.toResourceInfo(path, name, ResourceType.DIRECTORY);
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
            String folderName = fileNameWithPath.substring(0, lastIndexOfSplitter + 1);
            String fileName1 = fileNameWithPath.substring(lastIndexOfSplitter + 1);
            return resourceInfoMapper.toResourceInfo(statObjectResponse.size(), folderName, fileName1, ResourceType.FILE);
        } catch (Exception e) {
            return null;
        }
    }
}
