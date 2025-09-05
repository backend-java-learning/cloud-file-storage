package com.example.models;

public final class ResourcePath {
    private final String fullPath;
    private final String folderName;
    private final String parentPath;

    private ResourcePath(String fullPath, String folderName, String parentPath) {
        this.fullPath = fullPath;
        this.folderName = folderName;
        this.parentPath = parentPath;
    }

    public static ResourcePath of(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        int lastSlash = fullPath.lastIndexOf("/");
        String folderName;
        String parentPath;
        if (lastSlash == -1 || fullPath.split("/").length == 1) {
            folderName = fullPath;
            parentPath = "";
        } else {
            folderName = fullPath.substring(lastSlash + 1);
            parentPath = fullPath.substring(0, lastSlash + 1);
        }

        return new ResourcePath(fullPath, folderName, parentPath);
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getParentPath() {
        return parentPath;
    }
}

