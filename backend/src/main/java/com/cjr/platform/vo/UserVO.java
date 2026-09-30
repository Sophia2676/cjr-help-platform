package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息(不含密码)
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private String disabilityType;
    private Integer disabilityLevel;
    private String role;
    private String bio;
    private LocalDateTime createTime;
}
