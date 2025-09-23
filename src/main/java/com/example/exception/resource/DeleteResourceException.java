package com.example.exception.resource;

import com.example.exception.StorageException;

public class DeleteResourceException extends StorageException {
    public DeleteResourceException(String message) {
        super(message);
    }
}
