package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情
 */
@Data
public class PostDetailVO {

    private Long id;
    private String title;
    private String content;
    private String category;
    private List<String> images;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private LocalDateTime createTime;
    /** 当前登录人是否已点赞(未登录为 false) */
    private Boolean isLiked;
    private AuthorVO author;
}
