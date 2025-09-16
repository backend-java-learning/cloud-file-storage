package com.example.controllers;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.exception.InvalidPathException;
import com.example.factory.ResourceServiceFactory;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.FileMetadataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final FileMetadataService fileMetadataService;

    @GetMapping(value = "/resource")
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(@AuthenticationPrincipal User user,
                                                                @RequestParam String path) {
        log.info("Received request to get resource info [{}]", path);
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        ResourceInfoResponse resourceInfo = resourceServiceFactory
                .create(storageKey.getResourceType())
                .getInfo(storageKey);
        return ResponseEntity.ok(resourceInfo);
    }

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceInfoResponse>> uploadFile(@AuthenticationPrincipal User user,
                                                                 @RequestParam String path,
                                                                 @RequestParam List<MultipartFile> object) {
        log.info("Received request to upload files [{}] by path [{}]", String.join(" | ", object.stream().map(MultipartFile::getOriginalFilename).toList()), path);
        if (!path.endsWith("/") && !path.isEmpty()) {
            throw new InvalidPathException("The path for folder have to end with '/'");
        }
        //TODO: check what happens here
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path + object.getFirst().getResource().getFilename());
        List<ResourceInfoResponse> resourceInfoResponses = resourceServiceFactory
                .create(storageKey.getResourceType())
                .upload(storageKey, object);
        return ResponseEntity.ok(resourceInfoResponses);
    }

    @GetMapping("/resource/download")
    private ResponseEntity<Resource> downloadResource(@AuthenticationPrincipal User user,
                                                      @RequestParam String path) {
        log.info("Received request to download resource [{}]", path);
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        DownloadResult result = resourceServiceFactory
                .create(storageKey.getResourceType())
                .download(storageKey);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.resource());
    }

    @GetMapping(value = "/resource/move")
    public ResponseEntity<ResourceInfoResponse> moveResource(@AuthenticationPrincipal User user,
                                                             @RequestParam String from,
                                                             @RequestParam String to) {
        log.info("Received request to move resource from [{}] to [{}]", from, to);
        StorageKey sourceStorageKey = StorageKey.parsePath(user.getId(), from);
        StorageKey targetStorageKey = StorageKey.parsePath(user.getId(), to);
        //TODO: add exception
        if (sourceStorageKey.getResourceType() != targetStorageKey.getResourceType()) {
            // throw
        }
        ResourceInfoResponse resourceInfoResponse = resourceServiceFactory
                .create(sourceStorageKey.getResourceType())
                .move(sourceStorageKey, targetStorageKey);
        return ResponseEntity.ok().body(resourceInfoResponse);
    }

    @DeleteMapping(value = "/resource")
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal User user, @RequestParam String path) {
        log.info("Received request to delete resource [{}]", path);
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        resourceServiceFactory
                .create(storageKey.getResourceType())
                .remove(storageKey);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/resource/search")
    private ResponseEntity<List<ResourceInfoResponse>> searchResource(@AuthenticationPrincipal User user,
                                                                      @RequestParam String query) {
        log.info("Received request to search resource for user with id [{}] by name [{}]", user.getId(), query);
        String key = StorageKey.getKey(user.getId());
        List<ResourceInfoResponse> searchedResources = fileMetadataService.findByKeyAndNameContaining(key, query);
        return ResponseEntity.ok(searchedResources);
    }
}
