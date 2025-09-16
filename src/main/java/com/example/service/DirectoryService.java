package com.example.service;

import com.example.dto.ResourceInfoDto;
import com.example.models.StorageKey;

import java.util.List;

public interface DirectoryService {
    ResourceInfoDto createEmptyFolder(StorageKey storageKey);
    List<ResourceInfoDto> getDirectoryDetails(StorageKey storageKey);
}
