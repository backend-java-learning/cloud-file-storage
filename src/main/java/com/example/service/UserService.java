package com.example.service;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.exception.InvalidCredentialsException;
import com.example.exception.UserAlreadyExistsException;
import com.example.mapper.UserMapper;
import com.example.models.User;
import com.example.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    public AuthorizedUserResponse createUser(AuthorizeUserRequest authorizeUserRequest) {
        try {
            User user = userMapper.toUser(authorizeUserRequest);
            user.setPassword(passwordEncoder.encode(authorizeUserRequest.getPassword()));
            User savedUser = userRepository.save(user);
            return userMapper.toAuthorizedUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Пользователь с таким логином уже существует");
        }
    }

    public AuthorizedUserResponse getInfoAboutCurrentUser(Authentication authentication) {
        return userMapper.toAuthorizedUserResponse(authentication.getName());
    }
}
