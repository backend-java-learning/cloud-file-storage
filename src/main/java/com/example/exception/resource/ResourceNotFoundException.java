package com.example.exception.resource;

import com.example.exception.StorageException;

public class ResourceNotFoundException extends StorageException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
