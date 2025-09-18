package com.example.controllers;

import com.example.dto.ResourceInfoDto;
import com.example.exception.resource.ResourceTypeException;
import com.example.factory.ResourceServiceFactory;
import com.example.models.StorageKey;
import com.example.models.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ResourceMoveController {

    private final ResourceServiceFactory resourceServiceFactory;

    @GetMapping(value = "/resource/move")
    public ResponseEntity<ResourceInfoDto> moveResource(@AuthenticationPrincipal User user,
                                                        @RequestParam String from,
                                                        @RequestParam String to) {
        log.info("Received request to move resource from [{}] to [{}]", from, to);
        StorageKey sourceStorageKey = StorageKey.parsePath(user.getId(), from);
        StorageKey targetStorageKey = StorageKey.parsePath(user.getId(), to);
        if (sourceStorageKey.getResourceType() != targetStorageKey.getResourceType()) {
            throw new ResourceTypeException("Source and target key must have the same resource type [FILE | DIRECTORY]");
        }
        ResourceInfoDto resourceInfo = resourceServiceFactory
                .create(sourceStorageKey.getResourceType())
                .move(sourceStorageKey, targetStorageKey);
        return ResponseEntity.ok().body(resourceInfo);
    }
}
