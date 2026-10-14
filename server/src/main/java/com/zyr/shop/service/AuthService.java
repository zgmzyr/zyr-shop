package com.zyr.shop.service;

import com.zyr.shop.common.BusinessException;
import com.zyr.shop.dto.LoginRequest;
import com.zyr.shop.dto.PasswordRequest;
import com.zyr.shop.dto.UserVO;
import com.zyr.shop.entity.SysUser;
import com.zyr.shop.repository.SysUserRepository;
import com.zyr.shop.security.JwtUtil;
import com.zyr.shop.security.LoginUser;
import com.zyr.shop.security.UserContext;
import com.zyr.shop.util.Md5Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 *
 * <p>职责：用户登录校验、生成 JWT、获取当前用户信息、修改密码、更新基本资料</p>
 *
 * <p>说明：登录态通过 JWT + Authorization 请求头传递；
 * 管理员后台与 Thymeleaf 前台共用同一套登录接口，由 JwtInterceptor 解析</p>
 *
 * @author zyr
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 用户数据访问 */
    private final SysUserRepository userRepository;

    /** JWT 工具 */
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     *
     * <p>校验流程：按用户名查询 → MD5 密码比对 → 账号状态校验 → 生成 JWT</p>
     *
     * @param request 登录参数
     * @return 包含 token 与用户信息的 Map
     */
    public Map<String, Object> login(LoginRequest request) {
        // 1. 按用户名查询
        SysUser user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BusinessException(400, "用户名或密码错误"));
        // 2. 密码 MD5 比对
        if (!Md5Util.md5(request.getPassword()).equals(user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        // 3. 账号状态校验
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
        // 4. 生成 JWT 令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("【登录认证】用户登录成功：{}（角色：{}）", user.getUsername(), user.getRole());

        // 5. 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", UserVO.from(user));
        return result;
    }

    /**
     * 获取当前登录用户信息
     *
     * @param loginUser 上下文登录用户
     * @return 用户视图对象
     */
    public UserVO getCurrentUser(LoginUser loginUser) {
        SysUser user = userRepository.findById(loginUser.id())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        return UserVO.from(user);
    }

    /**
     * 修改个人密码
     *
     * <p>需先校验原密码，新密码再做 MD5 后入库</p>
     *
     * @param request   修改密码参数
     * @param loginUser 当前登录用户
     */
    public void changePassword(PasswordRequest request, LoginUser loginUser) {
        SysUser user = userRepository.findById(loginUser.id())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (!Md5Util.md5(request.getOldPassword()).equals(user.getPassword())) {
            throw new BusinessException(400, "原密码不正确");
        }
        user.setPassword(Md5Util.md5(request.getNewPassword()));
        userRepository.save(user);
        log.info("【登录认证】用户修改密码成功：{}", user.getUsername());
    }

    /**
     * 更新个人基本资料（昵称、手机号、收货地址）
     *
     * @param nickname  新昵称（可选）
     * @param phone     新手机号（可选）
     * @param address   新收货地址（可选）
     * @return 更新后的用户信息
     */
    public UserVO updateProfile(String nickname, String phone, String address) {
        SysUser user = userRepository.findById(UserContext.getUserId())
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (address != null) {
            user.setAddress(address);
        }
        return UserVO.from(userRepository.save(user));
    }
}
