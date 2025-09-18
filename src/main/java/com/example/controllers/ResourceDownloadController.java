package com.example.controllers;

import com.example.dto.DownloadResult;
import com.example.factory.ResourceServiceFactory;
import com.example.models.StorageKey;
import com.example.models.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ResourceDownloadController {

    private final ResourceServiceFactory resourceServiceFactory;

    @GetMapping("/resource/download")
    private ResponseEntity<StreamingResponseBody> downloadResource(@AuthenticationPrincipal User user,
                                                                   @RequestParam String path) {
        log.info("Received request to download resource [{}]", path);
        StorageKey storageKey = StorageKey.parsePath(user.getId(), path);
        DownloadResult result = resourceServiceFactory
                .create(storageKey.getResourceType())
                .downloadStream(storageKey);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.out());
    }
}
