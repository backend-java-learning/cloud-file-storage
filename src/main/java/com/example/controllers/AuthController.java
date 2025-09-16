package com.example.controllers;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.service.AuthorizationService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private UserService userService;
    private AuthorizationService authorizationService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthorizedUserResponse> signUp(@RequestBody AuthorizeUserRequest authorizeUserRequest) {
        log.info("Received sign up request: {}", authorizeUserRequest.getUsername());
        AuthorizedUserResponse createdUser = userService.registerUser(authorizeUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthorizedUserResponse> signIn(@RequestBody AuthorizeUserRequest authorizeUserRequest,
                                                         HttpServletRequest request) {
        log.info("Received sign in request: {}", authorizeUserRequest.getUsername());
        AuthorizedUserResponse authorizedUser = authorizationService.authenticate(authorizeUserRequest);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        return ResponseEntity.ok(authorizedUser);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut() {
        //TODO: implements
       // log.info("Received sign up request: {}", authorizeUserRequest.getUsername());
        return ResponseEntity.ok().build();
    }
}
