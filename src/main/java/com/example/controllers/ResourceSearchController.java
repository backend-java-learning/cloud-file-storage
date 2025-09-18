package com.example.controllers;

import com.example.dto.ResourceInfoDto;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.FileMetadataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ResourceSearchController {

    private final FileMetadataService fileMetadataService;

    @GetMapping("/resource/search")
    private ResponseEntity<List<ResourceInfoDto>> searchResource(@AuthenticationPrincipal User user,
                                                                 @RequestParam String query) {
        log.info("Received request to search resource for user with id [{}] by name [{}]", user.getId(), query);
        String key = StorageKey.getKey(user.getId());
        List<ResourceInfoDto> searchedResources = fileMetadataService.findByKeyAndNameContaining(key, query);
        return ResponseEntity.ok(searchedResources);
    }
}
