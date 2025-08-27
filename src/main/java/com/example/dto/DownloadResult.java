package com.example.dto;

import org.springframework.core.io.Resource;

//@Data
//@AllArgsConstructor
public record DownloadResult(String fileName, Resource resource, long size, String contentType) {
}
