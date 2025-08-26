package com.example.service;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.exception.UserAlreadyExistsException;
import com.example.mapper.UserMapper;
import com.example.models.User;
import com.example.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private MinioService minioService;

    @Transactional
    public AuthorizedUserResponse registerUser(AuthorizeUserRequest authorizeUserRequest) {
        String folderName = "";
        try {
            User user = userMapper.toUser(authorizeUserRequest);
            user.setPassword(passwordEncoder.encode(authorizeUserRequest.getPassword()));
            User savedUser = userRepository.save(user);
            folderName = "user-%s-files/".formatted(savedUser.getId());
            minioService.createFolder(folderName);
            return userMapper.toAuthorizedUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Пользователь с таким логином уже существует");
        } catch (Exception ex) {
            // если bucket был создан частично → попытка удалить
            try {
                minioService.removeFolder(folderName);
            } catch (Exception deleteException) {
                // логируем, но БД всё равно откатится
                //log.error("Не удалось удалить bucket посл
                // е ошибки", ex);
            }
            //Add MinioException
            throw new RuntimeException("Ошибка при создании bucket в MinIO", ex);
        }
    }

    public AuthorizedUserResponse getInfoAboutCurrentUser(Authentication authentication) {
        return userMapper.toAuthorizedUserResponse(authentication.getName());
    }
}
