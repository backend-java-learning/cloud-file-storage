package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.service.UploadService;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UploadController {

    private UploadService uploadService;

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceInfoResponse>> uploadFile(@RequestParam MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        List<ResourceInfoResponse> resourceInfoResponses = uploadService.uploadFile("user-files", "user-1-files/createdFolder", file);
        return ResponseEntity.ok(resourceInfoResponses);
    }
}
