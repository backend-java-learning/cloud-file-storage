package com.example.models;

import com.example.dto.enums.ResourceType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class StorageKey {

    private static final String KEY_REGEX_PATTERN = "^user-\\d+-files/$";
    private static final String KEY_PATTERN = "user-%s-files/";

    private final String key;
    private final String prefix;
    private final String objectName;
    private final String path;
    private final ResourceType resourceType;

    private StorageKey(int userId, String prefix, String objectName) {
        this(KEY_PATTERN.formatted(userId), prefix, objectName);
    }

    private StorageKey(String key, String prefix, String objectName) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key must not be null or blank");
        }
        if (!key.matches(KEY_REGEX_PATTERN)) {
            throw new IllegalArgumentException("Invalid key format: %s. Expected format: user-{id}-files/".formatted(key));
        }
        this.key = key;
        this.prefix = prefix == null ? "" : prefix;
        this.objectName = objectName == null ? "" : objectName;
        this.path = this.prefix + this.objectName;
        this.resourceType = (key + path).endsWith("/")
                ? ResourceType.DIRECTORY
                : ResourceType.FILE;
    }

    public String buildKey() {
        return key + path;
    }

    private static StorageKey parse(String key, String path) {
        List<String> parts = new ArrayList<>(Arrays.asList(path.split("/")));
        return parse(key, parts, path.endsWith("/"));
    }

    private static StorageKey parse(String key, List<String> parts, boolean isDir) {
        if (parts.isEmpty() || parts.getFirst().isBlank()) {
            return new StorageKey(key, "", "");
        }
        String objectName = isDir
                ? parts.removeLast() + "/"
                : parts.removeLast();
        String prefix = parts.isEmpty()
                ? ""
                : String.join("/", parts) + "/";
        return new StorageKey(key, prefix, objectName);
    }

    public static StorageKey parsePath(String fullPath) {
        List<String> pathList = new ArrayList<>(Arrays.asList(fullPath.split("/")));
        String key = pathList.removeFirst() + "/";
        return parse(key, pathList, fullPath.endsWith("/"));
    }

    public static StorageKey parsePath(int userId, String path) {
        return parse(KEY_PATTERN.formatted(userId), path);
    }

    public static StorageKey createEmptyDirectoryKey(int userId) {
        return new StorageKey(userId, "", "");
    }

    public static StorageKey createEmptyDirectoryKey(StorageKey storageKey) {
        return new StorageKey(storageKey.getKey(), storageKey.getPrefix(), "");
    }
}

