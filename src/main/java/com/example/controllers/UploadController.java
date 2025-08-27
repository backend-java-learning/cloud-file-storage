package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.exception.InvalidPathException;
import com.example.models.User;
import com.example.service.UploadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UploadController {

    private UploadService uploadService;

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceInfoResponse>> uploadFile(@AuthenticationPrincipal User user,
                                                                 @RequestParam String path,
                                                                 @RequestParam MultipartFile file) {
        if (!path.endsWith("/") && !path.isEmpty()) {
            throw new InvalidPathException("The path for folder have to end with '/'");
        }
        List<ResourceInfoResponse> resourceInfoResponses = uploadService.uploadFile(user.getId(), "user-files", path, file);
        return ResponseEntity.ok(resourceInfoResponses);
    }
}
