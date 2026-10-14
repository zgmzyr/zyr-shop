package com.zyr.shop.common;

import lombok.Getter;

/**
 * 业务异常类
 *
 * <p>业务校验不通过时主动抛出，由全局异常处理器转换为友好的错误提示</p>
 *
 * @author zyr
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误状态码 */
    private final Integer code;

    /**
     * 构造业务异常（默认 500 状态码）
     *
     * @param message 异常提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造业务异常
     *
     * @param code    错误状态码
     * @param message 异常提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
