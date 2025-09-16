package com.example.controllers;

import com.example.dto.AuthorizedUserResponse;
import com.example.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@Slf4j
public class UserController {

    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<AuthorizedUserResponse> getCurrentUser(Authentication authentication) {
        log.info("Received request to get information about current user with name [{}]", authentication.getName());
        AuthorizedUserResponse authorizedUser = userService.getInfoAboutCurrentUser(authentication);
        return ResponseEntity.ok(authorizedUser);
    }
}
