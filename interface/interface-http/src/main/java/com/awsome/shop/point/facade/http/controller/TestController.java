package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.test.TestDTO;
import com.awsome.shop.point.application.api.dto.test.request.*;
import com.awsome.shop.point.application.api.service.test.TestApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test 增删改查 Controller
 */
@Tag(name = "Test", description = "测试表增删改查")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TestController {

    private final TestApplicationService testApplicationService;

    @Operation(summary = "查询详情")
    @PostMapping("/public/test/get")
    public Result<TestDTO> get(@RequestBody @Valid GetTestRequest request) {
        return Result.success(testApplicationService.get(request));
    }

    @Operation(summary = "分页查询")
    @PostMapping("/public/test/list")
    public Result<PageResult<TestDTO>> list(@RequestBody @Valid ListTestRequest request) {
        return Result.success(testApplicationService.list(request));
    }

    @Operation(summary = "创建")
    @PostMapping("/public/test/create")
    public Result<TestDTO> create(@RequestBody @Valid CreateTestRequest request) {
        return Result.success(testApplicationService.create(request));
    }

    @Operation(summary = "更新")
    @PostMapping("/public/test/update")
    public Result<TestDTO> update(@RequestBody @Valid UpdateTestRequest request) {
        return Result.success(testApplicationService.update(request));
    }

    @Operation(summary = "删除")
    @PostMapping("/public/test/delete")
    public Result<Void> delete(@RequestBody @Valid DeleteTestRequest request) {
        testApplicationService.delete(request);
        return Result.success();
    }
}
