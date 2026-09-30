package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐助记录表
 */
@Data
@TableName("donation")
public class Donation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 求助ID */
    private Long helpRequestId;

    /** 捐助人ID */
    private Long donorId;

    /** 捐助金额(元) */
    private BigDecimal amount;

    /** 爱心留言 */
    private String message;

    /** 是否匿名 0否1是 */
    private Integer isAnonymous;

    /** 状态 1已捐助2受助人已确认 */
    private Integer status;

    /** 受助人确认时间 */
    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
