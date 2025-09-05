package com.example.models;

import com.example.dto.enums.ResourceType;

public record StorageKey(int userId, String relativePath) {
    public String buildKey() {
        String prefix = getPrefix();
        return relativePath.startsWith(prefix) ? relativePath : prefix + relativePath;
    }

    public String getPrefix() {
        return "user-%s-files/".formatted(userId);
    }

    public ResourceType getResourceType() {
        return relativePath.endsWith("/")
                ? ResourceType.DIRECTORY
                : ResourceType.FILE;
    }
}

