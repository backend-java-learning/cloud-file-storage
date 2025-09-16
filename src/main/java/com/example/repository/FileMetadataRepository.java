package com.example.repository;

import com.example.models.ResourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<ResourceInfo, Integer> {

    Optional<ResourceInfo> findByKeyAndPathAndName(String key, String path, String name);

    List<ResourceInfo> findByKeyAndPath(String key, String path);

    List<ResourceInfo> findByKeyAndNameContainingIgnoreCase(String key, String name);

    void deleteByKeyAndPath(String key, String path);
}
