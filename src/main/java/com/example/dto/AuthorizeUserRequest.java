package com.example.dto;

import lombok.Data;

@Data
public class AuthorizeUserRequest {
    private String username;
    private String password;
}
