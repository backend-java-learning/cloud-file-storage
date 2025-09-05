package com.example.service.domain;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import com.example.exception.ResourceNotFoundException;
import com.example.mapper.ResourceInfoMapper;
import com.example.models.StorageKey;
import com.example.service.StorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DirectoryService implements ResourceService {

    private StorageService storageService;
    private ResourceInfoMapper resourceInfoMapper;

    @Override
    public ResourceInfoResponse getInfo(StorageKey storageKey) {
        if (!storageService.doesObjectExist(storageKey)) {
            log.error("Directory [{}] doesn't exist", storageKey.buildKey());
            throw new ResourceNotFoundException("Directory [%s] doesn't exist".formatted(storageKey.relativePath()));
        }

        String folderName = storageKey.relativePath();
        String trimmed = folderName.endsWith("/")
                ? folderName.substring(0, folderName.length() - 1)
                : folderName;
        int lastSlash = trimmed.lastIndexOf("/");

        //TODO: move in utils
        String path;
        String name;

        if (lastSlash == -1) {
            // Если слешей нет, значит путь пустой, а всё остальное — имя
            path = "";
            name = folderName;
        } else {
            path = folderName.substring(0, lastSlash + 1); // включая последний "/"
            name = folderName.substring(lastSlash + 1);    // имя без "/"

            // Добавляем "/" к имени, если в исходном пути он был
            if (folderName.endsWith("/")) {
                name = name + "/";
            }
        }

        return resourceInfoMapper.toResourceInfo(path, name, ResourceType.DIRECTORY);
    }

    @Override
    public void remove() {

    }

    @Override
    public void download() {

    }

    @Override
    public ResourceInfoResponse rename() {
        return null;
    }
}
