package com.example.controllers;

import com.example.service.MinioService;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FileStorageController {

    private MinioService minioService;

    @GetMapping(value = "/resource", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> getResourceInfo(@AuthenticationPrincipal User user, @RequestBody String path) {
        // List<String> list = minioService.getListOfBuckets();
        StatObjectResponse object = minioService.getObject("folder/Меню на 21 день.pdf");
        StatObjectResponse object1 = minioService.getObject("folder/");
        //object.
        return ResponseEntity.ok(object);
    }
}
