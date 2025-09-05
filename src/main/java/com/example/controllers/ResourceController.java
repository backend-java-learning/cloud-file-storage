package com.example.controllers;

import com.example.dto.DownloadResult;
import com.example.dto.ResourceInfoResponse;
import com.example.exception.InvalidPathException;
import com.example.factory.ResourceServiceFactory;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.DownloadService;
import com.example.service.RenameService;
import com.example.service.ResourceInfoService;
import com.example.service.UploadService;
import com.example.service.domain.ResourceService;
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

    private RenameService renameService;
    private UploadService uploadService;

    @GetMapping(value = "/resource")
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(@AuthenticationPrincipal User user, @RequestParam String path) {
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = new StorageKey(user.getId(), path);
        ResourceService resourceService = resourceServiceFactory.create(storageKey.getResourceType());
        ResourceInfoResponse resourceInfo = resourceService.getInfo(storageKey);
        return ResponseEntity.ok(resourceInfo);
    }

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceInfoResponse>> uploadFile(@AuthenticationPrincipal User user,
                                                                 @RequestParam String path,
                                                                 @RequestParam MultipartFile file) {
        if (!path.endsWith("/") && !path.isEmpty()) {
            throw new InvalidPathException("The path for folder have to end with '/'");
        }
        List<ResourceInfoResponse> resourceInfoResponses = uploadService.uploadFile(user.getId(), path, file);
        return ResponseEntity.ok(resourceInfoResponses);
    }

    @GetMapping("/resource/download")
    private ResponseEntity<Resource> downloadResource(@AuthenticationPrincipal User user,
                                                      @RequestParam String path) {
        StorageKey storageKey = new StorageKey(user.getId(), path);
        DownloadResult result = resourceServiceFactory.create(storageKey.getResourceType()).download(storageKey);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.resource());
    }

    @GetMapping(value = "/resource/move")
    public ResponseEntity<ResourceInfoResponse> moveResource(@AuthenticationPrincipal User user,
                                                             @RequestParam String from,
                                                             @RequestParam String to) {
        ResourceInfoResponse resourceInfoResponse = renameService.moveResource(user.getId(), from, to);
        return ResponseEntity.ok().body(resourceInfoResponse);
    }

    @DeleteMapping(value = "/resource")
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal User user, @RequestParam String path) {
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        StorageKey storageKey = new StorageKey(user.getId(), path);
        resourceServiceFactory.create(storageKey.getResourceType()).remove(storageKey);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
