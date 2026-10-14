package com.zyr.shop.security;

import com.zyr.shop.config.ZyrProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌工具类
 *
 * <p>负责登录令牌的生成、解析与校验（HS256 算法）</p>
 *
 * @author zyr
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    /** JWT 中用户ID的声明键 */
    private static final String CLAIM_USER_ID = "userId";
    /** JWT 中用户名的声明键 */
    private static final String CLAIM_USERNAME = "username";
    /** JWT 中角色的声明键 */
    private static final String CLAIM_ROLE = "role";

    /** 项目配置 */
    private final ZyrProperties properties;

    /** 签名密钥对象 */
    private SecretKey secretKey;

    /**
     * 初始化 HMAC-SHA256 签名密钥
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT 令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return 签名后的 JWT 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        long expireMillis = properties.getJwt().getExpireHours() * 3600L * 1000L;
        Date now = new Date();
        return Jwts.builder()
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT 令牌并转换为登录用户对象
     *
     * @param token JWT 字符串
     * @return 登录用户信息
     * @throws io.jsonwebtoken.JwtException 令牌无效或过期时抛出
     */
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = claims.get(CLAIM_USER_ID, Long.class);
        String username = claims.get(CLAIM_USERNAME, String.class);
        String role = claims.get(CLAIM_ROLE, String.class);
        return new LoginUser(userId, username, role);
    }
}
