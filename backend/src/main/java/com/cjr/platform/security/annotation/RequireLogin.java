package com.cjr.platform.security.annotation;

import java.lang.annotation.*;

/**
 * 需登录才能访问(类/方法级)
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
