package com.example.mapper;

import com.example.dto.AuthorizeUserRequest;
import com.example.dto.AuthorizedUserResponse;
import com.example.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "login", source = "username")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toUser(AuthorizeUserRequest signUpRequest);

    @Mapping(target = "username", source = "login")
    AuthorizedUserResponse toAuthorizedUserResponse(User user);
}
