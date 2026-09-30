package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 残联政策资讯表
 */
@Data
@TableName("policy")
public class Policy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 摘要(列表页展示) */
    private String summary;

    private String content;

    /** 封面图路径 */
    private String coverImage;

    /** 来源(如:市残联) */
    private String source;

    /** 发布时间 */
    private LocalDateTime publishTime;

    private Integer viewCount;

    /** 状态 1已发布0下架 */
    private Integer status;

    /** 发布管理员ID */
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
