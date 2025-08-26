package com.example.exception;

public class DeleteResourceException extends StorageException {
    public DeleteResourceException(String message) {
        super(message);
    }

    public DeleteResourceException(String key, Throwable throwable) {
        super("Deletion error: The object [%s] could not be removed from storage due to an unexpected issue.".formatted(key), throwable);
    }
}
