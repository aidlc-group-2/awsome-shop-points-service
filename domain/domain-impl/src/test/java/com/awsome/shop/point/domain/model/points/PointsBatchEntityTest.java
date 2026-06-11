package com.awsome.shop.point.domain.model.points;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PointsBatchEntity 单元测试
 */
class PointsBatchEntityTest {

    private PointsBatchEntity batch(long remaining) {
        PointsBatchEntity batch = new PointsBatchEntity();
        batch.setUserId(1L);
        batch.setAmount(remaining);
        batch.setRemaining(remaining);
        batch.setStatus(BatchStatus.ACTIVE);
        return batch;
    }

    @Test
    @DisplayName("consume 剩余充足时全额消耗")
    void consumeShouldDeductFullAmount() {
        PointsBatchEntity batch = batch(100L);

        long consumed = batch.consume(40L);

        assertThat(consumed).isEqualTo(40L);
        assertThat(batch.getRemaining()).isEqualTo(60L);
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.ACTIVE);
    }

    @Test
    @DisplayName("consume 超过剩余时按剩余截断")
    void consumeShouldCapAtRemaining() {
        PointsBatchEntity batch = batch(30L);

        long consumed = batch.consume(100L);

        assertThat(consumed).isEqualTo(30L);
        assertThat(batch.getRemaining()).isZero();
    }

    @Test
    @DisplayName("consume 耗尽时状态变为 EXHAUSTED")
    void consumeShouldMarkExhaustedWhenEmpty() {
        PointsBatchEntity batch = batch(50L);

        batch.consume(50L);

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.EXHAUSTED);
    }

    @Test
    @DisplayName("isExpired 依据 expireAt 判断，null 表示永不过期")
    void isExpiredShouldCheckExpireAt() {
        PointsBatchEntity neverExpires = batch(10L);
        neverExpires.setExpireAt(null);
        assertThat(neverExpires.isExpired()).isFalse();

        PointsBatchEntity expired = batch(10L);
        expired.setExpireAt(LocalDateTime.now().minusDays(1));
        assertThat(expired.isExpired()).isTrue();

        PointsBatchEntity active = batch(10L);
        active.setExpireAt(LocalDateTime.now().plusDays(1));
        assertThat(active.isExpired()).isFalse();
    }

    @Test
    @DisplayName("markExpired 应置状态为 EXPIRED")
    void markExpiredShouldSetStatus() {
        PointsBatchEntity batch = batch(10L);

        batch.markExpired();

        assertThat(batch.getStatus()).isEqualTo(BatchStatus.EXPIRED);
    }
}
