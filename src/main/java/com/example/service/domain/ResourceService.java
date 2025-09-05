package com.example.service.domain;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;

public interface ResourceService {
    ResourceInfoResponse getInfo(StorageKey storageKey);
    void remove(StorageKey storageKey);
    DownloadResult download(StorageKey storageKey);
    ResourceInfoResponse rename();
}
