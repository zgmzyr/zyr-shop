package com.zyr.shop.common;

import lombok.Data;

/**
 * 统一接口返回结果包装类
 *
 * <p>所有 REST 接口统一返回 {@code {code, message, data}} 结构，便于前端按 code 判断成功失败</p>
 *
 * @param <T> 业务数据类型
 * @author zyr
 */
@Data
public class Result<T> {

    /** 状态码：200 成功，其他为失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /** 无参构造（框架反序列化使用） */
    public Result() {
    }

    /**
     * 全参构造
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    业务数据
     */
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造成功返回（带数据）
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 统一结果对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 构造成功返回（无数据）
     *
     * @param <T> 数据类型
     * @return 统一结果对象
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 构造失败返回
     *
     * @param code    错误状态码
     * @param message 错误提示
     * @param <T>     数据类型
     * @return 统一结果对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
