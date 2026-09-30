package com.cjr.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论最长500字")
    private String content;

    /** 父评论ID, 0=一级评论 */
    private Long parentId = 0L;

    /** 被回复人ID */
    private Long replyUserId;
}
