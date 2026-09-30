package com.cjr.platform.common;

/**
 * 统一响应状态码
 */
public interface ResultCode {

    /** 成功 */
    int SUCCESS = 200;
    /** 业务失败(参数错误/业务规则不满足) */
    int FAIL = 400;
    /** 未登录或登录已失效 */
    int UNAUTHORIZED = 401;
    /** 无权限 */
    int FORBIDDEN = 403;
    /** 资源不存在 */
    int NOT_FOUND = 404;
    /** 系统异常 */
    int ERROR = 500;
}
