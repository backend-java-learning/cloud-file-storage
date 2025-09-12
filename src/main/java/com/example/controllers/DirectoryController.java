package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.domain.DirectoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DirectoryController {

    private DirectoryService directoryService;

    @GetMapping(value = "/directory")
    public ResponseEntity<List<ResourceInfoResponse>> getDirectoryDetails(@AuthenticationPrincipal User user, @RequestParam String path) {
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        List<ResourceInfoResponse> directoryDetails = directoryService.getDirectoryDetails(storageKey);
        return ResponseEntity.ok(directoryDetails);
    }

    @PostMapping(value = "/directory")
    public ResponseEntity<ResourceInfoResponse> createEmptyFolder(@AuthenticationPrincipal User user, @RequestParam String path) {
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        ResourceInfoResponse folderInfo = directoryService.createEmptyFolder(storageKey);
        return ResponseEntity.ok(folderInfo);
    }
}
