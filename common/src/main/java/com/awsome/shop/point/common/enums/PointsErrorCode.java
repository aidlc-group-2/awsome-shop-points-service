package com.awsome.shop.point.common.enums;

/**
 * 积分业务错误码
 */
public enum PointsErrorCode implements ErrorCode {

    /** 积分余额不足 */
    INSUFFICIENT_BALANCE("POINTS_001", "积分余额不足"),

    /** 积分账户不存在 */
    ACCOUNT_NOT_FOUND("NOT_FOUND_002", "积分账户不存在"),

    /** 积分规则不存在 */
    RULE_NOT_FOUND("NOT_FOUND_003", "积分规则配置不存在"),

    /** 扣减金额非法 */
    INVALID_DEDUCT_AMOUNT("PARAM_001", "扣减金额必须大于0"),

    /** 发放金额非法 */
    INVALID_GRANT_AMOUNT("PARAM_002", "发放金额必须大于0"),

    /** 重复操作（幂等校验） */
    DUPLICATE_OPERATION("CONFLICT_002", "重复操作: {0}");

    private final String code;
    private final String message;

    PointsErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
