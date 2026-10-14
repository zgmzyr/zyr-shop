package com.zyr.shop.security;

import com.zyr.shop.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录认证拦截器
 *
 * <p>职责：</p>
 * <ol>
 *     <li>从请求头 Authorization（Bearer xxx）或查询参数 token（SSE/Thymeleaf 场景）解析登录令牌</li>
 *     <li>校验通过后把登录用户放入 {@link UserContext}</li>
 *     <li>方法上存在 {@link RequireAdmin} 注解时校验管理员角色</li>
 * </ol>
 *
 * @author zyr
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    /** 请求头中令牌前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** JWT 工具 */
    private final JwtUtil jwtUtil;

    /**
     * 请求前置处理：登录校验与角色校验
     *
     * @param request  当前请求
     * @param response 响应对象
     * @param handler  目标处理器
     * @return true 放行，false 拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非 Controller 方法（如静态资源）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 1. 提取令牌：优先请求头，其次请求参数（EventSource 无法自定义请求头；Thymeleaf 前台通过 cookie 写入但请求头仍可用）
        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            throw new BusinessException(401, "未登录或登录已过期，请重新登录");
        }

        // 2. 解析令牌
        LoginUser loginUser;
        try {
            loginUser = jwtUtil.parseToken(token);
        } catch (Exception e) {
            throw new BusinessException(401, "登录令牌无效或已过期，请重新登录");
        }
        UserContext.set(loginUser);

        // 3. 管理员权限校验
        if (handlerMethod.hasMethodAnnotation(RequireAdmin.class) && !UserContext.isAdmin()) {
            throw new BusinessException(403, "没有访问权限，该操作仅管理员可执行");
        }
        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal，防止线程复用导致用户信息串用
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 从请求中解析 JWT 令牌
     *
     * @param request 当前请求
     * @return 令牌字符串，不存在时返回 null
     */
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        // SSE 的 EventSource 与部分 Thymeleaf fetch 场景只能通过查询参数传令牌
        return request.getParameter("token");
    }
}
