package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.exception.InvalidPathException;
import com.example.models.User;
import com.example.service.ResourceInfoService;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ResourceController {

    private ResourceInfoService resourceInfoService;

    @GetMapping(value = "/resource")
    public ResponseEntity<ResourceInfoResponse> getResourceInfo(@AuthenticationPrincipal User user, @RequestParam String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        ResourceInfoResponse resourceInfo = resourceInfoService.getResourceInfo("user-files", path, user.getId());
        return ResponseEntity.ok(resourceInfo);
    }

    @DeleteMapping(value = "/resource")
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal User user, @RequestParam String path) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if (path.isEmpty()) {
            throw new InvalidPathException("Resource path can't be empty");
        }
        resourceInfoService.deleteResource("user-files", path, user.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
