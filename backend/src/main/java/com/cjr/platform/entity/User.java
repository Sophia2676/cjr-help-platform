package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("`user`")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名 */
    private String username;

    /** BCrypt密码哈希 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 头像路径 */
    private String avatar;

    private String phone;

    /** 微信openid(微信登录) */
    private String openid;

    private String email;

    /** 性别 0未知1男2女 */
    private Integer gender;

    /** 残疾类别 */
    private String disabilityType;

    /** 残疾等级1-4级 */
    private Integer disabilityLevel;

    /** 角色 USER/ADMIN */
    private String role;

    /** 账号状态 1正常0禁用 */
    private Integer status;

    /** 个人简介 */
    private String bio;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 0否1是 */
    @TableLogic
    private Integer deleted;
}
