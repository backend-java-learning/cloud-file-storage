package com.example.controllers;

import com.example.dto.ResourceInfoDto;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.DirectoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class DirectoryController {

    private DirectoryService directoryService;

    @GetMapping(value = "/directory")
    public ResponseEntity<List<ResourceInfoDto>> getDirectoryDetails(@AuthenticationPrincipal User user, @RequestParam String path) {
        log.info("Received request to get directory details [{}]", path);
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        List<ResourceInfoDto> directoryDetails = directoryService.getDirectoryDetails(storageKey);
        return ResponseEntity.ok(directoryDetails);
    }

    @PostMapping(value = "/directory")
    public ResponseEntity<ResourceInfoDto> createEmptyFolder(@AuthenticationPrincipal User user, @RequestParam String path) {
        log.info("Received request to create empty directory [{}]", path);
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        ResourceInfoDto folderInfo = directoryService.createEmptyFolder(storageKey);
        return ResponseEntity.ok(folderInfo);
    }
}
