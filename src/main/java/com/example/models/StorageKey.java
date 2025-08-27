package com.example.models;

public record StorageKey(int userId, String relativePath) {
    public String buildKey() {
        String prefix = "user-%s-files/".formatted(userId);
        return relativePath.startsWith(prefix) ? relativePath : prefix + relativePath;
    }
}

