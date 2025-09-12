package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;

import java.util.List;

public interface DirectoryService {
    ResourceInfoResponse createEmptyFolder(StorageKey storageKey);
    List<ResourceInfoResponse> getDirectoryDetails(StorageKey storageKey);
}
