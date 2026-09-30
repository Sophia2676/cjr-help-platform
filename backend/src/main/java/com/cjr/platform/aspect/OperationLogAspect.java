package com.cjr.platform.aspect;

import com.cjr.platform.entity.OperationLog;
import com.cjr.platform.security.LoginUser;
import com.cjr.platform.security.UserContext;
import com.cjr.platform.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志切面: 记录操作人/IP/URI/参数(密码字段脱敏)/耗时/异常, 写入 operation_log 表
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int PARAMS_MAX_LEN = 2000;
    private static final int MSG_MAX_LEN = 500;

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint pjp, com.cjr.platform.aspect.OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        OperationLog entity = new OperationLog();

        LoginUser user = UserContext.get();
        if (user != null) {
            entity.setUserId(user.getId());
            entity.setUsername(user.getUsername());
        }
        entity.setOperation(operationLog.value());
        entity.setMethod(pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName());

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            entity.setRequestMethod(request.getMethod());
            entity.setRequestUri(request.getRequestURI());
            entity.setIp(getIp(request));
        }
        entity.setRequestParams(sanitizeParams(pjp.getArgs()));

        try {
            Object result = pjp.proceed();
            entity.setStatus(1);
            return result;
        } catch (Throwable t) {
            entity.setStatus(0);
            entity.setErrorMsg(truncate(t.getMessage(), MSG_MAX_LEN));
            throw t;
        } finally {
            entity.setCostTime(System.currentTimeMillis() - start);
            try {
                operationLogService.save(entity);
            } catch (Exception e) {
                log.error("操作日志写入失败", e);
            }
        }
    }

    /** 序列化方法参数: 过滤请求/响应/文件对象, 密码字段脱敏, 截断长度 */
    private String sanitizeParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> safe = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null
                    || arg instanceof HttpServletRequest
                    || arg instanceof HttpServletResponse
                    || arg instanceof MultipartFile
                    || arg instanceof BindingResult) {
                continue;
            }
            safe.add(arg);
        }
        if (safe.isEmpty()) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(safe.size() == 1 ? safe.get(0) : safe);
            json = json.replaceAll("(\"(?:password|oldPassword|newPassword|confirmPassword|token)\"\\s*:\\s*)\"[^\"]*\"", "$1\"***\"");
            return truncate(json, PARAMS_MAX_LEN);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            int idx = ip.indexOf(',');
            if (idx > 0) {
                ip = ip.substring(0, idx).trim();
            }
        }
        return ip;
    }
}
