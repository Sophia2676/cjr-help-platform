package com.cjr.platform.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理: 业务异常/参数校验异常/404/兜底系统异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = firstError(e.getBindingResult().getFieldErrors());
        return Result.fail(msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = firstError(e.getBindingResult().getFieldErrors());
        return Result.fail(msg);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Result<Void> handleNotFound(Exception e) {
        return Result.fail(ResultCode.NOT_FOUND, "接口不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.ERROR, "系统繁忙，请稍后重试");
    }

    private String firstError(java.util.List<FieldError> errors) {
        return errors.isEmpty() ? "参数错误" : errors.get(0).getDefaultMessage();
    }
}
