package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.exception.InvalidPathException;
import com.example.models.User;
import com.example.service.ResourceInfoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DirectoryController {

    private ResourceInfoService resourceInfoService;

    @GetMapping(value = "/directory")
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(@AuthenticationPrincipal User user, @RequestParam String path) {
        ResourceInfoResponse resourceInfo = resourceInfoService.getResourceInfo(path, user.getId());
        return ResponseEntity.ok(resourceInfo);
    }
}
