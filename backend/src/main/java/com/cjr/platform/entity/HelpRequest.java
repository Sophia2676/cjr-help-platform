package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐助求助表(一对一爱心捐助)
 */
@Data
@TableName("help_request")
public class HelpRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 求助人ID */
    private Long userId;

    private String title;

    private String description;

    /** 目标金额(元) */
    private BigDecimal targetAmount;

    /** 已筹金额(元) */
    private BigDecimal raisedAmount;

    /** 捐助人次 */
    private Integer donateCount;

    /** 证明材料图片 英文逗号分隔 */
    private String images;

    /** 联系电话(仅展示给登录用户) */
    private String contactPhone;

    /** 状态 0待审核1募捐中2已完成3已驳回 */
    private Integer status;

    private String rejectReason;

    /** 完成时间 */
    private LocalDateTime completeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
