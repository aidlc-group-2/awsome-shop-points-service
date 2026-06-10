package com.awsome.shop.point.application.impl.service.points;

import com.awsome.shop.point.application.api.dto.points.ExpiringPointsDTO;
import com.awsome.shop.point.application.api.dto.points.PointsBalanceDTO;
import com.awsome.shop.point.application.api.dto.points.PointsRuleDTO;
import com.awsome.shop.point.application.api.dto.points.PointsTransactionDTO;
import com.awsome.shop.point.application.api.dto.points.request.*;
import com.awsome.shop.point.application.api.service.points.PointsApplicationService;
import com.awsome.shop.point.common.dto.PageResult;
import com.awsome.shop.point.domain.model.points.GrantType;
import com.awsome.shop.point.domain.model.points.PointsBatchEntity;
import com.awsome.shop.point.domain.model.points.PointsRuleEntity;
import com.awsome.shop.point.domain.model.points.PointsTransactionEntity;
import com.awsome.shop.point.domain.service.points.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分应用服务实现
 *
 * <p>只依赖 Domain Service，不直接依赖 Repository</p>
 */
@Service
@RequiredArgsConstructor
public class PointsApplicationServiceImpl implements PointsApplicationService {

    private final PointsAccountDomainService pointsAccountDomainService;
    private final PointsGrantDomainService pointsGrantDomainService;
    private final PointsTransactionDomainService pointsTransactionDomainService;
    private final PointsRuleDomainService pointsRuleDomainService;
    private final PointsExpiryDomainService pointsExpiryDomainService;

    // ==================== 员工端 ====================

    @Override
    public PointsBalanceDTO getBalance(GetBalanceRequest request) {
        Long userId = Long.parseLong(request.getOperatorId());
        long balance = pointsAccountDomainService.getBalance(userId);

        PointsBalanceDTO dto = new PointsBalanceDTO();
        dto.setUserId(userId);
        dto.setBalance(balance);
        return dto;
    }

    @Override
    public PageResult<PointsTransactionDTO> listTransactions(ListTransactionRequest request) {
        // 如果指定了 userId（管理员查其他用户），用指定的；否则用自己的
        Long targetUserId = request.getUserId() != null
                ? request.getUserId()
                : Long.parseLong(request.getOperatorId());

        PageResult<PointsTransactionEntity> page = pointsTransactionDomainService.pageByUserId(
                targetUserId, request.getPage(), request.getSize());

        return page.convert(this::toTransactionDTO);
    }

    @Override
    public ExpiringPointsDTO getExpiringPoints(GetBalanceRequest request) {
        Long userId = Long.parseLong(request.getOperatorId());
        List<PointsBatchEntity> expiringBatches = pointsExpiryDomainService.getExpiringBatches(userId, 30);

        ExpiringPointsDTO dto = new ExpiringPointsDTO();
        long totalExpiring = expiringBatches.stream()
                .mapToLong(PointsBatchEntity::getRemaining)
                .sum();
        dto.setExpiringAmount(totalExpiring);
        dto.setBatchCount(expiringBatches.size());
        dto.setEarliestExpireAt(expiringBatches.isEmpty() ? null : expiringBatches.get(0).getExpireAt());
        return dto;
    }

    // ==================== 管理员端 ====================

    @Override
    public void adjustPoints(AdjustPointsRequest request) {
        Long operatorId = Long.parseLong(request.getOperatorId());
        pointsGrantDomainService.adjust(
                request.getUserId(), request.getDelta(), request.getReason(), operatorId);
    }

    @Override
    public PointsRuleDTO getRule() {
        PointsRuleEntity rule = pointsRuleDomainService.getRule();
        return toRuleDTO(rule);
    }

    @Override
    public void updateRule(UpdateRuleRequest request) {
        pointsRuleDomainService.updateRule(
                request.getOnboardingBonus(),
                request.getPeriodicAmount(),
                request.getPeriodicCycle(),
                request.getValidityDays());
    }

    // ==================== 内部接口 ====================

    @Override
    public void grant(GrantPointsRequest request) {
        GrantType grantType = GrantType.valueOf(request.getGrantType());
        pointsGrantDomainService.grant(
                request.getUserId(), request.getAmount(), grantType, request.getReason());
    }

    @Override
    public void deduct(DeductPointsRequest request) {
        pointsGrantDomainService.deduct(
                request.getUserId(), request.getAmount(), request.getOrderRef());
    }

    @Override
    public void refund(RefundPointsRequest request) {
        pointsGrantDomainService.refund(
                request.getUserId(), request.getAmount(), request.getOrderRef());
    }

    // ==================== 转换方法 ====================

    private PointsTransactionDTO toTransactionDTO(PointsTransactionEntity entity) {
        PointsTransactionDTO dto = new PointsTransactionDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType().name());
        dto.setAmount(entity.getAmount());
        dto.setBalanceAfter(entity.getBalanceAfter());
        dto.setOrderRef(entity.getOrderRef());
        dto.setReason(entity.getReason());
        dto.setOperatorId(entity.getOperatorId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private PointsRuleDTO toRuleDTO(PointsRuleEntity entity) {
        PointsRuleDTO dto = new PointsRuleDTO();
        dto.setId(entity.getId());
        dto.setOnboardingBonus(entity.getOnboardingBonus());
        dto.setPeriodicAmount(entity.getPeriodicAmount());
        dto.setPeriodicCycle(entity.getPeriodicCycle());
        dto.setValidityDays(entity.getValidityDays());
        return dto;
    }
}
