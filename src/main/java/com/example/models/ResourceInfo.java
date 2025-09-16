package com.example.models;

import com.example.dto.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "files_metadata")
@Setter
@Getter
public class ResourceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "path")
    private String path;

    @Column(name = "name")
    private String name;

    @Column(name = "size")
    private Double size;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ResourceType type;
}
