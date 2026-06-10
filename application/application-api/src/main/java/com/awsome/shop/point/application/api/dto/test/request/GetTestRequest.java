package com.awsome.shop.point.application.api.dto.test.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 获取 Test 详情请求
 */
@Data
public class GetTestRequest {

    @NotNull(message = "ID不能为空")
    private Long id;
}
