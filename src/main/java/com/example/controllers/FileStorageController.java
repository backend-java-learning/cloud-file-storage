package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.models.User;
import com.example.service.ResourceInfoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FileStorageController {

    private ResourceInfoService resourceInfoService;

    @GetMapping(value = "/resource")
    public ResponseEntity<?> getResourceInfo(@AuthenticationPrincipal User user, @RequestParam String path) {
        int userId = user.getId();
        ResourceInfoResponse resourceInfo = resourceInfoService.getResourceInfo(path);
        return ResponseEntity.ok(resourceInfo);
    }
}
