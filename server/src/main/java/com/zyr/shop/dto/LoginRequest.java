package com.zyr.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 *
 * <p>管理员后台与普通用户前台共用此 DTO 调用 {@code /api/auth/login}</p>
 *
 * @author zyr
 */
@Data
public class LoginRequest {

    /** 登录用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录密码（明文，服务端会做 MD5 比对） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
