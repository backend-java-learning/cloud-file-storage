package com.example.integration;

import com.example.dto.AuthorizeUserRequest;
import com.example.models.StorageKey;
import com.example.models.User;
import com.example.repository.UserRepository;
import com.example.service.minio.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class RegistrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StorageService storageService;

    private List<Integer> createdUserIds = new ArrayList<>();

    @Test
    void registerUser() throws Exception {

        AuthorizeUserRequest authorizeUserRequest = new AuthorizeUserRequest();
        authorizeUserRequest.setUsername("test2");
        authorizeUserRequest.setPassword("secretPassword");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(authorizeUserRequest.getUsername()));

        Optional<User> createdUser = userRepository.findByLogin(authorizeUserRequest.getUsername());
        assertTrue(createdUser.isPresent());
        boolean doesUserFolderExist = storageService.doesObjectExist(StorageKey.createEmptyDirectoryKey(createdUser.get().getId()));
        assertTrue(doesUserFolderExist);
        createdUserIds.add(createdUser.get().getId());
    }

    @Test
    void registerAsExistedUser() throws Exception {
        AuthorizeUserRequest authorizeUserRequest = new AuthorizeUserRequest();
        authorizeUserRequest.setUsername("test");
        authorizeUserRequest.setPassword("secretPassword");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(authorizeUserRequest.getUsername()));

        Optional<User> createdUser = userRepository.findByLogin(authorizeUserRequest.getUsername());
        assertTrue(createdUser.isPresent());
        boolean doesUserFolderExist = storageService.doesObjectExist(StorageKey.createEmptyDirectoryKey(createdUser.get().getId()));
        assertTrue(doesUserFolderExist);

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
        createdUserIds.add(createdUser.get().getId());
    }

    @Test
    void registerUserWithInvalidPassword() throws Exception {
        AuthorizeUserRequest authorizeUserRequest = new AuthorizeUserRequest();
        authorizeUserRequest.setUsername("test2");
        authorizeUserRequest.setPassword("test2");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizeUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        Optional<User> createdUser = userRepository.findByLogin(authorizeUserRequest.getUsername());
        assertTrue(createdUser.isEmpty());
    }

    @AfterEach
    void cleanupFileMetadataDB() {
        for(Integer userId : createdUserIds) {
            storageService.removeObjects(userId);
        }
    }
}
