package com.example.exception;

import java.util.List;

public class DeleteResourceException extends StorageException {
    public DeleteResourceException(String key) {
        super("Deletion error: The object [%s] could not be removed from storage due to an unexpected issue.".formatted(key));
    }

    public DeleteResourceException(List<String> key) {
        super("Deletion error: The object [%s] could not be removed from storage due to an unexpected issue.".formatted(String.join("; ", key)));
    }

    public DeleteResourceException(String key, Throwable throwable) {
        super("Deletion error: The object [%s] could not be removed from storage due to an unexpected issue.".formatted(key), throwable);
    }
}
