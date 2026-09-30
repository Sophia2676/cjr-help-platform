package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子评论表(两级: 一级评论+回复)
 */
@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    /** 评论人ID */
    private Long userId;

    /** 父评论ID 0=一级评论 */
    private Long parentId;

    /** 被回复人ID */
    private Long replyUserId;

    private String content;

    /** 状态 1正常0已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
