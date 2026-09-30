package com.cjr.platform.controller;

import com.cjr.platform.aspect.OperationLog;
import com.cjr.platform.common.Result;
import com.cjr.platform.common.BusinessException;
import com.cjr.platform.dto.LoginDTO;
import com.cjr.platform.dto.PhoneLoginDTO;
import com.cjr.platform.dto.RegisterDTO;
import com.cjr.platform.dto.WechatLoginDTO;
import com.cjr.platform.security.annotation.RequireLogin;
import com.cjr.platform.service.UserService;
import com.cjr.platform.vo.LoginVO;
import com.cjr.platform.vo.UserVO;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @OperationLog("用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok("注册成功", null);
    }

    @PostMapping("/login")
    @OperationLog("用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok("登录成功", userService.login(dto));
    }

    /** 发送短信验证码(演示模式：验证码直接返回，生产环境接入短信服务商) */
    @PostMapping("/sms-code")
    public Result<Map<String, String>> smsCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        Map<String, String> data = new HashMap<>();
        data.put("phone", phone);
        data.put("code", userService.sendSmsCode(phone));
        return Result.ok("验证码已发送（演示模式）", data);
    }

    /** 手机号登录(密码或验证码) */
    @PostMapping("/phone-login")
    @OperationLog("手机号登录")
    public Result<LoginVO> phoneLogin(@Valid @RequestBody PhoneLoginDTO dto) {
        return Result.ok("登录成功", userService.phoneLogin(dto));
    }

    /** 微信扫码登录(开放平台回调 code 换登录态) */
    @PostMapping("/wechat-login")
    @OperationLog("微信登录")
    public Result<LoginVO> wechatLogin(@Valid @RequestBody WechatLoginDTO dto) {
        return Result.ok("登录成功", userService.wechatLogin(dto.getCode()));
    }

    @GetMapping("/info")
    @RequireLogin
    public Result<UserVO> info() {
        return Result.ok(userService.getCurrentInfo());
    }

    @PostMapping("/logout")
    @RequireLogin
    @OperationLog("退出登录")
    public Result<Void> logout() {
        // JWT 无状态, 前端删除本地 token 即可
        return Result.ok();
    }
}
