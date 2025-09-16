package com.example.service;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoDto;
import com.example.models.StorageKey;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResourceService {
    ResourceInfoDto getInfo(StorageKey storageKey);
    void remove(StorageKey storageKey);
    DownloadResult download(StorageKey storageKey);
    ResourceInfoDto move(StorageKey sourcePrefix, StorageKey targetPrefix);
    List<ResourceInfoDto> upload(StorageKey storageKey, List<MultipartFile> file);
}
