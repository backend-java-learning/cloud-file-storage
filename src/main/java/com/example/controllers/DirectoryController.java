package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.domain.DirectoryResourceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DirectoryController {

    private DirectoryResourceService directoryResourceService;

    @GetMapping(value = "/directory")
    public ResponseEntity<List<ResourceInfoResponse>> getResourceInfo(@AuthenticationPrincipal User user, @RequestParam String path) {
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        List<ResourceInfoResponse> resourcesInfo = directoryResourceService.getDirectoryDetails(storageKey);
        return ResponseEntity.ok(resourcesInfo);
    }
}
