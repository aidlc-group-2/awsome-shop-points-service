package com.awsome.shop.point.domain.model.test;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Test 领域实体
 */
@Data
public class TestEntity {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
