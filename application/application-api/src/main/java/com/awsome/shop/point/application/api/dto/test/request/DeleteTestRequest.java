package com.awsome.shop.point.application.api.dto.test.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除 Test 请求
 */
@Data
public class DeleteTestRequest {

    @NotNull(message = "ID不能为空")
    private Long id;
}
