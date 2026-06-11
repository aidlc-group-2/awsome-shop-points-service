package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.points.request.DeductPointsRequest;
import com.awsome.shop.point.application.api.dto.points.request.GrantPointsRequest;
import com.awsome.shop.point.application.api.dto.points.request.RefundPointsRequest;
import com.awsome.shop.point.application.api.service.points.PointsApplicationService;
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
 * 积分服务内部接口 Controller（供其他微服务调用）
 *
 * <p>路径前缀 /api/v1/private/ 表示内部接口，不经过前端</p>
 */
@Tag(name = "积分内部接口", description = "供认证服务和兑换服务调用的内部接口")
@RestController
@RequestMapping("/api/v1/private/point")
@RequiredArgsConstructor
public class InternalPointsController {

    private final PointsApplicationService pointsApplicationService;

    @Operation(summary = "发放积分（入职奖励/周期性/手动）")
    @PostMapping("/grant")
    public Result<Void> grant(@RequestBody @Valid GrantPointsRequest request) {
        pointsApplicationService.grant(request);
        return Result.success();
    }

    @Operation(summary = "扣减积分（兑换时）")
    @PostMapping("/deduct")
    public Result<Void> deduct(@RequestBody @Valid DeductPointsRequest request) {
        pointsApplicationService.deduct(request);
        return Result.success();
    }

    @Operation(summary = "退回积分（兑换取消时）")
    @PostMapping("/refund")
    public Result<Void> refund(@RequestBody @Valid RefundPointsRequest request) {
        pointsApplicationService.refund(request);
        return Result.success();
    }
}
