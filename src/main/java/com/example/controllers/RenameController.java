package com.example.controllers;

import com.example.dto.ResourceInfoResponse;
import com.example.models.User;
import com.example.service.RenameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class RenameController {

    private RenameService renameService;

    @GetMapping(value = "/resource/move")
    public ResponseEntity<ResourceInfoResponse> moveResource(@AuthenticationPrincipal User user,
                                                             @RequestParam String from,
                                                             @RequestParam String to) {
        ResourceInfoResponse resourceInfoResponse = renameService.moveResource(user.getId(), from, to);
        return ResponseEntity.ok().body(resourceInfoResponse);
    }

}
