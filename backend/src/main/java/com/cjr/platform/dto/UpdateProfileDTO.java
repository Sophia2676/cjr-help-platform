package com.cjr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 20, message = "昵称最长20字")
    private String nickname;

    @Size(max = 20, message = "真实姓名最长20字")
    private String realName;

    private String avatar;

    /** 0未知1男2女 */
    private Integer gender;

    private String disabilityType;

    /** 1-4级 */
    private Integer disabilityLevel;

    @Size(max = 20, message = "手机号格式不正确")
    private String phone;

    @Size(max = 100, message = "邮箱格式不正确")
    private String email;

    @Size(max = 500, message = "个人简介最长500字")
    private String bio;
}
