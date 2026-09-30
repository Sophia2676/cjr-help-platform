package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息表
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收人ID */
    private Long receiverId;

    /** 发送人ID NULL=系统 */
    private Long senderId;

    /** 类型 AUDIT审核结果/DONATION捐助动态/SYSTEM系统通知 */
    private String type;

    private String title;

    private String content;

    /** 关联业务ID(帖子/求助ID,供前端跳转) */
    private Long relatedId;

    /** 是否已读 0否1是 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
