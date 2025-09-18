package com.example.service.minio;

import com.example.models.StorageKey;
import io.minio.*;
import io.minio.messages.DeleteObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.List;

public class MinioArgsFactory {

    @Value("${MINIO_BUCKET}")
    private String bucket;

    public PutObjectArgs putObjectArgs(StorageKey key, InputStream is, long size, String contentType) {
        return PutObjectArgs.builder()
                .bucket(bucket)
                .object(key.buildKey())
                .stream(is, size, -1)
                .contentType(contentType)
                .build();
    }

    public GetObjectArgs getObjectArgs(StorageKey key) {
        return getObjectArgs(key.buildKey());
    }

    public GetObjectArgs getObjectArgs(String objectName) {
        return GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build();
    }

    public CopyObjectArgs copyObjectArgs(StorageKey source, StorageKey target) {
        return CopyObjectArgs.builder()
                .bucket(bucket)
                .object(target.buildKey())
                .source(CopySource.builder()
                        .bucket(bucket)
                        .object(source.buildKey())
                        .build())
                .build();
    }

    public RemoveObjectArgs removeObjectArgs(StorageKey key) {
        return RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(key.buildKey())
                .build();
    }

    public RemoveObjectsArgs removeObjectsArgs(List<DeleteObject> objectsToDelete) {
        return RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(objectsToDelete)
                .build();
    }

    public StatObjectArgs statObjectArgs(StorageKey key) {
        return StatObjectArgs.builder()
                .bucket(bucket)
                .object(key.buildKey())
                .build();
    }

    public ListObjectsArgs listObjectsArgs(String prefix, boolean isRecursive) {
        return ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(isRecursive)
                .build();
    }
}
