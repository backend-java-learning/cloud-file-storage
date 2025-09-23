package com.example.exception.resource;

import com.example.exception.StorageException;

public class ResourceAlreadyExist extends StorageException {
    public ResourceAlreadyExist(String message) {
        super(message);
    }
}
