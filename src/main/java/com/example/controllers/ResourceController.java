package com.example.controllers;

import com.example.dto.ResourceInfoDto;
import com.example.exception.InvalidPathException;
import com.example.factory.ResourceServiceFactory;
import com.example.models.StorageKey;
import com.example.models.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ResourceController {

    private final ResourceServiceFactory resourceServiceFactory;

    @GetMapping(value = "/resource")
    public ResponseEntity<ResourceInfoDto> getResourceInfo(@AuthenticationPrincipal User user,
                                                           @RequestParam String path) {
        log.info("Received request to get resource info [{}]", path);
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        ResourceInfoDto resourceInfo = resourceServiceFactory.create(storageKey.getResourceType())
                .getInfo(storageKey);
        return ResponseEntity.ok(resourceInfo);
    }

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceInfoDto>> uploadFile(@AuthenticationPrincipal User user,
                                                            @RequestParam String path,
                                                            @RequestParam List<MultipartFile> object) {
        log.info("Received request to upload files [{}] by path [{}]", String.join(" | ", object.stream().map(MultipartFile::getOriginalFilename).toList()), path);
        if (!path.endsWith("/") && !path.isEmpty()) {
            throw new InvalidPathException("The path for folder have to end with '/'");
        }
        //TODO: check what happens here
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path + object.getFirst().getResource().getFilename());
        List<ResourceInfoDto> resourceInfo = resourceServiceFactory.create(storageKey.getResourceType())
                .upload(storageKey, object);
        return ResponseEntity.ok(resourceInfo);
    }

    @DeleteMapping(value = "/resource")
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal User user, @RequestParam String path) {
        log.info("Received request to delete resource [{}]", path);
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        resourceServiceFactory.create(storageKey.getResourceType())
                .remove(storageKey);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
