package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final FileMetadataService fileMetadataService;

    @GetMapping("/resource/search")
    private ResponseEntity<List<ResourceInfoResponse>> searchResource(@AuthenticationPrincipal User user,
                                                                      @RequestParam String query) {
        String key = StorageKey.getKey(user.getId());
        List<ResourceInfoResponse> searchedResources = fileMetadataService.findByKeyAndNameContaining(key, query);
        return ResponseEntity.ok(searchedResources);
    }
}
