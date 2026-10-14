package com.zyr.shop.security;

/**
 * 当前登录用户上下文
 *
 * <p>基于 ThreadLocal 在一次请求线程内共享登录用户信息，
 * 请求结束后由 JwtInterceptor 负责清理，避免内存泄漏</p>
 *
 * @author zyr
 */
public final class UserContext {

    /** 线程私有变量，保存当前登录用户 */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /** 私有构造，禁止实例化 */
    private UserContext() {
    }

    /**
     * 设置当前登录用户
     *
     * @param user 登录用户
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户
     *
     * @return 登录用户，未登录时返回 null
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.id();
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是管理员返回 true
     */
    public static boolean isAdmin() {
        LoginUser user = HOLDER.get();
        return user != null && "ADMIN".equals(user.role());
    }

    /** 清理当前线程的登录用户 */
    public static void clear() {
        HOLDER.remove();
    }
}
