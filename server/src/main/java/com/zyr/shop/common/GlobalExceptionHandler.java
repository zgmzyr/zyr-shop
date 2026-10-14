package com.zyr.shop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * <p>统一拦截 Controller 抛出的异常，转换为标准 Result 结构返回给前端，
 * 避免直接把堆栈信息暴露给页面</p>
 *
 * @author zyr
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 标准错误结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 校验失败）
     *
     * @param e 校验异常
     * @return 携带字段级错误信息的结果，便于前端逐字段提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidException(MethodArgumentNotValidException e) {
        // 收集每个字段的错误提示
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        Result<Map<String, String>> result = Result.error(400, "参数校验不通过");
        result.setData(errors);
        return result;
    }

    /**
     * 兜底：处理其他未预期的异常
     *
     * @param e 未知异常
     * @return 标准错误结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统繁忙，请稍后再试：" + e.getMessage());
    }
}
