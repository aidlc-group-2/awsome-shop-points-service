package com.awsome.shop.point.bootstrap.scheduler;

import com.awsome.shop.point.domain.service.points.PointsExpiryDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 积分定时任务调度器
 *
 * <p>包含两个定时任务：
 * 1. 积分过期处理：每天凌晨 2:00 执行
 * 2. 周期性发放：每月 1 号凌晨 3:00 执行
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointsScheduler {

    private final PointsExpiryDomainService pointsExpiryDomainService;

    /**
     * 积分过期处理 — 每天凌晨 2:00 执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void expirePoints() {
        log.info("===== 开始执行积分过期处理定时任务 =====");
        try {
            int count = pointsExpiryDomainService.expirePoints();
            log.info("===== 积分过期处理完成，处理批次数: {} =====", count);
        } catch (Exception e) {
            log.error("积分过期处理异常", e);
        }
    }

    /**
     * 周期性发放 — 每月 1 号凌晨 3:00 执行
     *
     * <p>注意：实际周期应根据 PointsRule.periodicCycle 配置决定。
     * 当前默认按月执行，如果需要按周/日执行，需要修改 cron 表达式或改为动态调度。</p>
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void periodicGrant() {
        log.info("===== 开始执行周期性积分发放定时任务 =====");
        try {
            int count = pointsExpiryDomainService.runPeriodicGrant();
            log.info("===== 周期性发放完成，发放用户数: {} =====", count);
        } catch (Exception e) {
            log.error("周期性发放异常", e);
        }
    }
}
