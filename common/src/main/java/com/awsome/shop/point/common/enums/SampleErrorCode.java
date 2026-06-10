package com.awsome.shop.point.common.enums;

/**
 * 示例业务错误码（仅供参考，请根据实际业务替换）
 *
 * <p>错误码前缀决定 HTTP 状态码映射，参见 {@link ErrorCode} 接口说明。</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * throw new BusinessException(SampleErrorCode.RESOURCE_NOT_FOUND);
 * throw new BusinessException(SampleErrorCode.RESOURCE_ALREADY_EXISTS, "order-123");
 * </pre>
 */
public enum SampleErrorCode implements ErrorCode {

    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND("NOT_FOUND_001", "资源不存在"),

    /**
     * 资源已存在（支持参数化消息）
     */
    RESOURCE_ALREADY_EXISTS("CONFLICT_001", "资源已存在: {0}"),

    /**
     * 操作不允许
     */
    OPERATION_NOT_ALLOWED("AUTHZ_001", "当前用户无权执行此操作");

    private final String code;
    private final String message;

    SampleErrorCode(String code, String message) {
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
