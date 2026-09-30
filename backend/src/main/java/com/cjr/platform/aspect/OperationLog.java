package com.cjr.platform.aspect;

import java.lang.annotation.*;

/**
 * 操作日志注解: 标注在 Controller 方法上, AOP 切面记录到 operation_log 表
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作描述(如: 发布帖子 / 审核帖子) */
    String value() default "";
}
