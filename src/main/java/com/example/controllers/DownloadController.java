package com.example.controllers;

import com.example.dto.DownloadResult;
import com.example.models.User;
import com.example.service.DownloadService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DownloadController {

    private DownloadService downloadService;

    @GetMapping("/resource/download")
    private ResponseEntity<Resource> downloadResource(@AuthenticationPrincipal User user,
                                                      @RequestParam String path) throws Exception {
        DownloadResult result = downloadService.download(user.getId(), path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\"")
                .body(result.resource());
    }
}
