package com.cjr.platform.security;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.cjr.platform.common.ResultCode;
import com.cjr.platform.entity.User;
import com.cjr.platform.mapper.UserMapper;
import com.cjr.platform.security.annotation.RequireAdmin;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.security.annotation.RequireWorker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

/**
 * 全局鉴权拦截器:
 * 1. 解析 Bearer token -> 查库校验用户存在/未禁用 -> 写入 UserContext(公开接口带合法 token 同样解析, 用于 isLiked 等个性化字段)
 * 2. 校验 @RequireLogin / @RequireAdmin 注解 -> 未过直接写 401/403 JSON
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        LoginUser loginUser = null;
        String auth = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(auth) && auth.startsWith("Bearer ")) {
            LoginUser parsed = jwtUtil.parseToken(auth.substring(7));
            if (parsed != null) {
                // 每请求查库校验, 保证禁用/删除用户立即生效
                User user = userMapper.selectById(parsed.getId());
                if (user != null && user.getStatus() == 1) {
                    loginUser = LoginUser.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .nickname(user.getNickname())
                            .role(user.getRole())
                            .build();
                } else {
                    writeJson(response, ResultCode.UNAUTHORIZED, "账号已被禁用或不存在，请联系管理员");
                    return false;
                }
            }
        }
        if (loginUser != null) {
            UserContext.set(loginUser);
        }

        RequireLogin requireLogin = getAnnotation(method, RequireLogin.class);
        RequireAdmin requireAdmin = getAnnotation(method, RequireAdmin.class);
        RequireWorker requireWorker = getAnnotation(method, RequireWorker.class);
        boolean needLogin = requireLogin != null || requireAdmin != null || requireWorker != null;
        if (needLogin && loginUser == null) {
            writeJson(response, ResultCode.UNAUTHORIZED, "请先登录");
            return false;
        }
        if (requireAdmin != null && !loginUser.isAdmin()) {
            writeJson(response, ResultCode.FORBIDDEN, "无权限访问");
            return false;
        }
        if (requireWorker != null && !loginUser.isWorker()) {
            writeJson(response, ResultCode.FORBIDDEN, "仅社区工作者可访问");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }

    private <A extends Annotation> A getAnnotation(HandlerMethod method, Class<A> type) {
        A a = method.getMethodAnnotation(type);
        if (a == null) {
            a = method.getBeanType().getAnnotation(type);
        }
        return a;
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
