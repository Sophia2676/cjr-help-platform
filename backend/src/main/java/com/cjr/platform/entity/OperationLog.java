package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表(AOP切面写入)
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作描述(如:发布帖子/审核帖子) */
    private String operation;

    /** 执行方法 类名.方法名 */
    private String method;

    /** HTTP方法 */
    private String requestMethod;

    /** 请求URI */
    private String requestUri;

    /** 请求参数JSON(已脱敏截断) */
    private String requestParams;

    /** 客户端IP */
    private String ip;

    /** 1成功0异常 */
    private Integer status;

    /** 异常信息 */
    private String errorMsg;

    /** 耗时(毫秒) */
    private Long costTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
