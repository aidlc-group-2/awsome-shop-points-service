-- ============================================================
-- 积分流水幂等约束：同一订单的同类操作（DEDUCT/REFUND）只允许一条
-- order_ref 为 NULL 的流水（GRANT/ADJUST/EXPIRE）不受约束（MySQL 唯一索引允许多个 NULL）
-- ============================================================
ALTER TABLE `points_transaction`
    ADD UNIQUE INDEX `uk_order_ref_type` (`order_ref`, `type`);
