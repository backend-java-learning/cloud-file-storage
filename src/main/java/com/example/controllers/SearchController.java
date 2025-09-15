package com.example.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SearchController {

//    @GetMapping("/resource/search")
//    private ResponseEntity<List<ResourceInfoResponse>> searchResource(@AuthenticationPrincipal User user,
//                                                                      @RequestParam String query) {
//        StorageKey storageKey = StorageKey.parsePath(user.getId(), query);
////        DownloadResult result =
////                resourceServiceFactory
////                .create(storageKey.getResourceType())
////                .download(storageKey);
//        return
////                ResponseEntity.ok()
////                .contentType(MediaType.APPLICATION_OCTET_STREAM)
////                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
////                .body(result.resource());
//    }
}
