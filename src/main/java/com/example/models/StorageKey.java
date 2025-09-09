package com.example.models;

import com.example.dto.enums.ResourceType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        this.resourceType = (this.objectName.isEmpty() || !this.objectName.endsWith("/"))
                ? ResourceType.FILE
                : ResourceType.DIRECTORY;
    }

    public String buildKey() {
        return key + path;
                //path.startsWith(key) ? path : key + path;
    }

    public StorageKey updatePrefix(String prefix) {
        return new StorageKey(key, prefix, objectName);
    }

    private static StorageKey parse(String key, String path) {
        List<String> parts = new ArrayList<>(Arrays.asList(path.split("/")));
        if (parts.isEmpty() || parts.getFirst().isBlank()) {
            throw new IllegalArgumentException("Invalid path: %s".formatted(path));
        }
        String objectName = path.endsWith("/")
                ? parts.removeLast() + "/"
                : parts.removeLast();
        String prefix = parts.isEmpty()
                ? ""
                : String.join("/", parts) + "/";
        return new StorageKey(key, prefix, objectName);
    }

    public static StorageKey parsePath(String fullPath) {
        List<String> path = new ArrayList<>(Arrays.asList(fullPath.split("/")));
        if (path.getFirst().matches("")) {
            //TODO: add exception
            //throw new
        }

        String key = path.getFirst();
        path.removeFirst();
        if (fullPath.endsWith("/")) {
            String objectName = path.getLast() + "/";
            path.removeLast();
            String prefix = path.isEmpty()
                    ? ""
                    : path.stream().collect(Collectors.joining("/", "", "/"));
            return new StorageKey(key, prefix, objectName);
        }

        String objectName = path.getLast();
        path.removeLast();
        String prefix = path.isEmpty()
                ? ""
                : path.stream().collect(Collectors.joining("/", "", "/"));
        return new StorageKey(key, prefix, objectName);
    }

    public static StorageKey parsePath(int userId, String path) {
        String key = "user-%s-files/".formatted(userId);
        List<String> pathList = new ArrayList<>(Arrays.asList(path.split("/")));
        if (path.endsWith("/")) {
            String objectName = pathList.getLast() + "/";
            pathList.removeLast();
            String prefix = pathList.isEmpty()
                    ? ""
                    : pathList.stream().collect(Collectors.joining("/", "", "/"));
            return new StorageKey(key, prefix, objectName);
        }
        String objectName = pathList.getLast();
        pathList.removeLast();
        String prefix = pathList.isEmpty()
                ? ""
                : pathList.stream().collect(Collectors.joining("/", "", "/"));
        return new StorageKey(key, prefix, objectName);
    }

    public static StorageKey createEmptyDirectoryKey(int userId) {
        return new StorageKey(userId, "", "");
    }
}

