package com.example.utils;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCreator {

    public StreamingResponseBody createZip(List<FileEntry> files) {
        return outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (FileEntry entry : files) {
                    try (InputStream is = entry.getInputStream()) {
                        zos.putNextEntry(new ZipEntry(entry.getEntryName()));
                        is.transferTo(zos);
                        zos.closeEntry();
                    }
                }
            }
        };
    }

    public static class FileEntry {
        private final String entryName;
        private final InputStream inputStream;

        public FileEntry(String entryName, InputStream inputStream) {
            this.entryName = entryName;
            this.inputStream = inputStream;
        }

        public String getEntryName() { return entryName; }
        public InputStream getInputStream() { return inputStream; }
    }
}
