package com.example.repository;

import com.example.models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Integer> {

    Optional<FileMetadata> findByKeyAndPathAndName(String key, String path, String name);

    List<FileMetadata> findByKeyAndPath(String key, String path);

    List<FileMetadata> findByKeyAndNameContaining(String key, String name);

    void deleteByKeyAndPath(String key, String path);
}
