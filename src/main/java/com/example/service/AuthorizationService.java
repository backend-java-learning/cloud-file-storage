package com.example.service;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.exception.InvalidCredentialsException;
import com.example.mapper.UserMapper;
import com.example.models.User;
import com.example.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorizationService {

    private AuthenticationManager authenticationManager;
    private UserMapper userMapper;
    private UserRepository userRepository;

    public AuthorizedUserResponse authenticate(AuthorizeUserRequest authorizeUserRequest) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authorizeUserRequest.getUsername(), authorizeUserRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        Optional<User> userOptional = userRepository.findByLogin(authorizeUserRequest.getUsername());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException("Неверный пользователь");
        }
        User user = userOptional.get();
        return userMapper.toAuthorizedUserResponse(user);
    }
}
