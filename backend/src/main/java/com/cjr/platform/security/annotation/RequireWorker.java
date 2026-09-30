package com.cjr.platform.security.annotation;

import java.lang.annotation.*;

/**
 * 需社区工作者(WORKER)或管理员(ADMIN)角色访问(类/方法级)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireWorker {
}
