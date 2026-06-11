package com.awsome.shop.point.application.api.service.points;

import com.awsome.shop.point.application.api.dto.points.ExpiringPointsDTO;
import com.awsome.shop.point.application.api.dto.points.PointsBalanceDTO;
import com.awsome.shop.point.application.api.dto.points.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.points.PointsTransactionDTO;
import com.awsome.shop.point.application.api.dto.points.request.*;
import com.awsome.shop.point.common.dto.PageResult;

/**
 * 积分应用服务接口
 */
public interface PointsApplicationService {

    // ==================== 员工端 ====================

    /**
     * 查询积分余额
     */
    PointsBalanceDTO getBalance(GetBalanceRequest request);

    /**
     * 查询积分变动记录
     */
    PageResult<PointsTransactionDTO> listTransactions(ListTransactionRequest request);

    /**
     * 查询即将过期的积分（30天内）
     */
    ExpiringPointsDTO getExpiringPoints(GetBalanceRequest request);

    // ==================== 管理员端 ====================

    /**
     * 手动调整用户积分
     */
    void adjustPoints(AdjustPointsRequest request);

    /**
     * 获取积分规则
     */
    PointsRuleDTO getRule();

    /**
     * 更新积分规则
     */
    void updateRule(UpdateRuleRequest request);

    // ==================== 内部接口（跨服务） ====================

    /**
     * 发放积分（auth-service 注册后调用）
     */
    void grant(GrantPointsRequest request);

    /**
     * 扣减积分（order-service 兑换时调用）
     */
    void deduct(DeductPointsRequest request);

    /**
     * 退回积分（order-service 取消时调用）
     */
    void refund(RefundPointsRequest request);
}
