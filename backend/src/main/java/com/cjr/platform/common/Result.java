package com.cjr.platform.common;

import lombok.Data;

/**
 * 统一响应体 {code, msg, data}
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS, msg, data);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(ResultCode.FAIL, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
