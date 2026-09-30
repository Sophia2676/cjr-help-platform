package com.cjr.platform.security;

/**
 * 当前登录用户上下文(ThreadLocal, 拦截器 afterCompletion 中清理防泄漏)
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 当前登录用户ID, 未登录返回 null */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
