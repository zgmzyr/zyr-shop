package com.zyr.shop.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员权限注解
 *
 * <p>标注在 Controller 方法上，JwtInterceptor 会校验当前登录用户必须为 ADMIN 角色，
 * 否则抛出 403 业务异常</p>
 *
 * @author zyr
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
}
