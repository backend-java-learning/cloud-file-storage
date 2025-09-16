package com.example.dto;

import com.example.dto.enums.ResourceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class ResourceInfoDto {
    private String path;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long size;
    private ResourceType type;
}
