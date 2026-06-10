package com.awsome.shop.point.application.api.dto.test;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Test 数据传输对象
 */
@Data
public class TestDTO {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
