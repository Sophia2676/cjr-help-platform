package com.cjr.platform.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户(JWT载荷 + 拦截器查库后写入 UserContext)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long id;
    private String username;
    private String nickname;
    private String role;

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isWorker() {
        return "WORKER".equals(role) || isAdmin();
    }
}
