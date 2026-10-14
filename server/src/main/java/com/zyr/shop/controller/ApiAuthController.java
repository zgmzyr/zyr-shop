package com.zyr.shop.controller;

import com.zyr.shop.common.Result;
import com.zyr.shop.dto.LoginRequest;
import com.zyr.shop.dto.PasswordRequest;
import com.zyr.shop.dto.UserVO;
import com.zyr.shop.security.UserContext;
import com.zyr.shop.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证相关接口
 *
 * <p>路径前缀 {@code /api/auth}，登录接口在 WebConfig 中已免鉴权</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *     <li>POST /api/auth/login  登录（管理员与普通用户共用）</li>
 *     <li>GET  /api/auth/info   获取当前登录用户信息</li>
 *     <li>PUT  /api/auth/password 修改个人密码</li>
 *     <li>PUT  /api/auth/profile  更新个人基本资料</li>
 * </ul>
 *
 * @author zyr
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    /** 认证服务 */
    private final AuthService authService;

    /**
     * 用户登录（免鉴权）
     *
     * @param request 登录参数
     * @return token 与用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(authService.getCurrentUser(UserContext.get()));
    }

    /**
     * 修改个人密码
     *
     * @param request 修改密码参数
     * @return 操作结果
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordRequest request) {
        authService.changePassword(request, UserContext.get());
        return Result.success();
    }

    /**
     * 更新个人基本资料
     *
     * @param nickname 新昵称（可选）
     * @param phone    新手机号（可选）
     * @param address  新收货地址（可选）
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String address) {
        return Result.success(authService.updateProfile(nickname, phone, address));
    }
}
