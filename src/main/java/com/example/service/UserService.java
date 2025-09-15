package com.example.service;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.exception.UserAlreadyExistsException;
import com.example.mapper.UserMapper;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private StorageService storageService;
    private DirectoryService directoryService;

    @Transactional
    public AuthorizedUserResponse registerUser(AuthorizeUserRequest authorizeUserRequest) {
        Integer savedUserId = null;
        try {
            User user = userMapper.toUser(authorizeUserRequest);
            user.setPassword(passwordEncoder.encode(authorizeUserRequest.getPassword()));
            User savedUser = userRepository.save(user);
            savedUserId = savedUser.getId();
            directoryService.createEmptyFolder(StorageKey.createEmptyDirectoryKey(savedUserId));
            return userMapper.toAuthorizedUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Пользователь с таким логином уже существует");
        } catch (Exception ex) {
            if (savedUserId != null) {
                storageService.removeObjects(savedUserId);
            }
            throw new RuntimeException("Unexpected", ex);
        }
    }

    public AuthorizedUserResponse getInfoAboutCurrentUser(Authentication authentication) {
        return userMapper.toAuthorizedUserResponse(authentication.getName());
    }
}
