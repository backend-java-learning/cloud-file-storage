package com.example.utils;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCreator {

//    public StreamingResponseBody createZip(List<FileEntry> files) {
//        return outputStream -> {
//            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
//                for (FileEntry entry : files) {
//                    try (InputStream is = entry.getInputStream().get()) {
//                        zos.putNextEntry(new ZipEntry(entry.getEntryName()));
//                        is.transferTo(zos);
//                        zos.closeEntry();
//                    }
//                }
//            }
//        };
//    }

    public StreamingResponseBody createZip(List<FileEntry> files) {
        return outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (FileEntry entry : files) {
                    String entryName = entry.getEntryName();
                    if (entryName == null || entryName.isEmpty()) continue;

                    // Если это папка (заканчивается на "/") — добавляем пустой ZipEntry
                    if (entryName.endsWith("/")) {
                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.closeEntry();
                        continue;
                    }

                    // Обычный файл
                    zos.putNextEntry(new ZipEntry(entryName));
                    try (InputStream is = entry.getInputStream().get()) {
                        is.transferTo(zos);
                    }
                    zos.closeEntry();
                }
            }
        };
    }


    public static class FileEntry {
        private final String entryName;
        private final Supplier<InputStream> inputStream;

        public FileEntry(String entryName, Supplier<InputStream> inputStream) {
            this.entryName = entryName;
            this.inputStream = inputStream;
        }

        public String getEntryName() { return entryName; }
        public Supplier<InputStream> getInputStream() { return inputStream; }
    }
}
