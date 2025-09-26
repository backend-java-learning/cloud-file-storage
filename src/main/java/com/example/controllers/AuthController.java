package com.example.controllers;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.service.AuthorizationService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private UserService userService;
    private AuthorizationService authorizationService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthorizedUserResponse> signUp(@Valid @RequestBody AuthorizeUserRequest authorizeUserRequest,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        log.info("Received sign up request: {}", authorizeUserRequest.getUsername());
        AuthorizedUserResponse createdUser = userService.registerUser(authorizeUserRequest, request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthorizedUserResponse> signIn(@Valid @RequestBody AuthorizeUserRequest authorizeUserRequest,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        log.info("Received sign in request: {}", authorizeUserRequest.getUsername());
        AuthorizedUserResponse authorizedUser = authorizationService.authenticate(authorizeUserRequest, request, response);
        return ResponseEntity.ok(authorizedUser);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpSession httpSession) {
        log.info("Received sign out request");
        httpSession.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
