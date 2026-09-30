package com.cjr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信扫码登录入参（web 开放平台回调的 code）
 */
@Data
public class WechatLoginDTO {

    @NotBlank(message = "微信授权code不能为空")
    private String code;
}
