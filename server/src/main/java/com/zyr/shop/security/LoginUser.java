package com.zyr.shop.security;

/**
 * 登录用户轻量对象（解析 JWT 后存入 ThreadLocal）
 *
 * @param id       用户ID
 * @param username 用户名
 * @param role     角色：ADMIN / USER
 * @author zyr
 */
public record LoginUser(Long id, String username, String role) {
}
