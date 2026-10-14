package com.zyr.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * @author zyr
 */
@Data
public class PasswordRequest {

    /** 原密码（明文） */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /** 新密码（明文，服务端保存时再做 MD5） */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
