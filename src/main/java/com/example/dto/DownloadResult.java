package com.example.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public record DownloadResult(String fileName, StreamingResponseBody out, String contentType) {
}
