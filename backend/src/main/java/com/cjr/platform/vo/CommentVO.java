package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论(一级评论含回复列表)
 */
@Data
public class CommentVO {

    private Long id;
    private Long postId;
    private Long parentId;
    private String content;
    /** 被回复人昵称(回复时展示) */
    private String replyTo;
    private LocalDateTime createTime;
    private AuthorVO user;
    /** 回复列表(仅一级评论填充) */
    private List<CommentVO> replies;
}
