package com.example.controllers;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.exception.InvalidPathException;
import com.example.factory.ResourceServiceFactory;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.UploadService;
import lombok.AllArgsConstructor;
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
public class ResourceController {

    private final ResourceServiceFactory resourceServiceFactory;

    private UploadService uploadService;

    @GetMapping(value = "/resource")
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(@AuthenticationPrincipal User user,
                                                                @RequestParam String path) {
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
        if (!path.endsWith("/") && !path.isEmpty()) {
            throw new InvalidPathException("The path for folder have to end with '/'");
        }
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path + object.getFirst().getResource().getFilename());
        List<ResourceInfoResponse> resourceInfoResponses = uploadService.uploadFile(storageKey, object);
        return ResponseEntity.ok(resourceInfoResponses);
    }

    @GetMapping("/resource/download")
    private ResponseEntity<Resource> downloadResource(@AuthenticationPrincipal User user,
                                                      @RequestParam String path) {
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
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        resourceServiceFactory
                .create(storageKey.getResourceType())
                .remove(storageKey);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
