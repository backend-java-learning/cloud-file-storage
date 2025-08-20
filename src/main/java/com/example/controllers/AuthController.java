package com.example.controllers;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthorizedUserResponse> signUp(@RequestBody AuthorizeUserRequest authorizeUserRequest) {
        AuthorizedUserResponse createdUser = userService.createUser(authorizeUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthorizedUserResponse> signIn() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut() {
        return ResponseEntity.ok().build();
    }
}
