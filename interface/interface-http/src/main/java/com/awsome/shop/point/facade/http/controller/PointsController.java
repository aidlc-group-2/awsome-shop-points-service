package com.awsome.shop.point.facade.http.controller;

import com.awsome.shop.point.application.api.dto.points.ExpiringPointsDTO;
import com.awsome.shop.point.application.api.dto.points.PointsBalanceDTO;
import com.awsome.shop.point.application.api.dto.points.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.points.PointsTransactionDTO;
import com.awsome.shop.point.application.api.dto.points.request.*;
import com.awsome.shop.point.application.api.service.points.PointsApplicationService;
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
 * 积分服务 Controller（员工端 + 管理员端）
 */
@Tag(name = "积分服务", description = "积分余额、变动记录、规则配置、手动调整")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PointsController {

    private final PointsApplicationService pointsApplicationService;

    // ==================== 员工端（public，网关不需要鉴权或仅需登录） ====================

    @Operation(summary = "查询积分余额")
    @PostMapping("/public/point/balance/get")
    public Result<PointsBalanceDTO> getBalance(@RequestBody @Valid GetBalanceRequest request) {
        return Result.success(pointsApplicationService.getBalance(request));
    }

    @Operation(summary = "查询积分变动记录")
    @PostMapping("/public/point/transaction/list")
    public Result<PageResult<PointsTransactionDTO>> listTransactions(
            @RequestBody @Valid ListTransactionRequest request) {
        return Result.success(pointsApplicationService.listTransactions(request));
    }

    @Operation(summary = "查询即将过期积分（30天内）")
    @PostMapping("/public/point/expiring/get")
    public Result<ExpiringPointsDTO> getExpiringPoints(@RequestBody @Valid GetBalanceRequest request) {
        return Result.success(pointsApplicationService.getExpiringPoints(request));
    }

    // ==================== 管理员端（protected，需要登录 + ADMIN 角色） ====================

    @Operation(summary = "手动调整用户积分")
    @PostMapping("/point/adjust")
    public Result<Void> adjustPoints(@RequestBody @Valid AdjustPointsRequest request) {
        pointsApplicationService.adjustPoints(request);
        return Result.success();
    }

    @Operation(summary = "获取积分规则配置")
    @PostMapping("/point/rule/get")
    public Result<PointsRuleDTO> getRule() {
        return Result.success(pointsApplicationService.getRule());
    }

    @Operation(summary = "更新积分规则配置")
    @PostMapping("/point/rule/update")
    public Result<Void> updateRule(@RequestBody @Valid UpdateRuleRequest request) {
        pointsApplicationService.updateRule(request);
        return Result.success();
    }

    @Operation(summary = "查看用户积分变动记录（管理员）")
    @PostMapping("/point/transaction/list")
    public Result<PageResult<PointsTransactionDTO>> listUserTransactions(
            @RequestBody @Valid ListTransactionRequest request) {
        return Result.success(pointsApplicationService.listTransactions(request));
    }
}
