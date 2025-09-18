package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthorizeUserRequest {
    @NotBlank(message = "Name can't be empty")
    private String username;
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;
}
