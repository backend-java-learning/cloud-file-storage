package com.example.exception.resource;

import com.example.exception.StorageException;

public class ResourceTypeException extends StorageException {
  public ResourceTypeException(String message) {
    super(message);
  }
}
