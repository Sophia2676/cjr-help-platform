package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 互助帖子表
 */
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者用户ID */
    private Long userId;

    private String title;

    private String content;

    /** 分类 */
    private String category;

    /** 图片路径列表 英文逗号分隔 */
    private String images;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    /** 审核状态 0待审核1已通过2已驳回 */
    private Integer status;

    /** 驳回原因 */
    private String rejectReason;

    /** 是否置顶 0否1是 */
    private Integer isTop;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
