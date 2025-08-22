package com.example.mapper;

import com.example.dto.ResourceInfoResponse;
import com.example.dto.enums.ResourceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResourceInfoMapper {

    @Mapping(target = "path", source = "path")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "size", source = "size")
    ResourceInfoResponse toResourceInfo(Long size, String path, String name, ResourceType type);
}
