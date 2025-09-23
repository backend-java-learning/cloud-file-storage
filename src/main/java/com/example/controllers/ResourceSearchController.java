package com.example.controllers;

import com.example.dto.ResourceInfoDto;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.service.storage.SearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class ResourceSearchController {

    private SearchService searchService;

    @GetMapping("/resource/search")
    @ResponseStatus(HttpStatus.OK)
    private List<ResourceInfoDto> searchResource(@AuthenticationPrincipal User user,
                                                 @RequestParam String query) {
        log.info("Received request to search resource for user with id [{}] by name [{}]", user.getId(), query);
        StorageKey key = StorageKey.createEmptyDirectoryKey(user.getId());
        return searchService.search(key, query);
    }
}
