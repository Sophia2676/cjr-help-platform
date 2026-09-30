package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子点赞表
 */
@Data
@TableName("post_like")
public class PostLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    /** 点赞人ID */
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
