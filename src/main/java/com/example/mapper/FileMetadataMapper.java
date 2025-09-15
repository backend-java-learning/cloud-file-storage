package com.example.mapper;

import com.example.models.FileMetadata;
import com.example.models.StorageKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileMetadataMapper {

    @Mapping(target = "path", source = "storageKey.prefix")
    @Mapping(target = "name", source = "storageKey.objectName")
    @Mapping(target = "type", source = "storageKey.resourceType")
    FileMetadata of(StorageKey storageKey, Long size);

    @Mapping(target = "path", source = "storageKey.prefix")
    @Mapping(target = "name", source = "storageKey.objectName")
    @Mapping(target = "type", source = "storageKey.resourceType")
    FileMetadata of(StorageKey storageKey);
}
