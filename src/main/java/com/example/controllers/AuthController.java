package com.example.controllers;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.service.AuthorizationService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;
    private AuthorizationService authorizationService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthorizedUserResponse> signUp(@RequestBody AuthorizeUserRequest authorizeUserRequest) {
        AuthorizedUserResponse createdUser = userService.registerUser(authorizeUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthorizedUserResponse> signIn(@RequestBody AuthorizeUserRequest authorizeUserRequest,
                                                         HttpServletRequest request) {
        AuthorizedUserResponse authorizedUser = authorizationService.authenticate(authorizeUserRequest);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        return ResponseEntity.ok(authorizedUser);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut() {
        return ResponseEntity.ok().build();
    }
}
