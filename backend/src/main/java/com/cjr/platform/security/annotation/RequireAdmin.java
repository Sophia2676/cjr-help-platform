package com.cjr.platform.security.annotation;

import java.lang.annotation.*;

/**
 * 需管理员角色才能访问(隐含需登录)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}
