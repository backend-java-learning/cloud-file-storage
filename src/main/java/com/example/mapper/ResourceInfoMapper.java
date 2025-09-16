package com.example.mapper;

import com.example.dto.ResourceInfoDto;
import com.example.models.FileMetadata;
import com.example.models.StorageKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResourceInfoMapper {

    @Mapping(target = "path", source = "storageKey.prefix")
    @Mapping(target = "name", source = "storageKey.objectName")
    @Mapping(target = "type", source = "storageKey.resourceType")
    @Mapping(target = "size", source = "size")
    ResourceInfoDto toResourceInfoDto(StorageKey storageKey, Long size);

    ResourceInfoDto toResourceInfoDto(FileMetadata fileMetadata);
}
