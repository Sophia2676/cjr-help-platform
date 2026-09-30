package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表项
 */
@Data
public class PostVO {

    private Long id;
    private String title;
    /** 内容摘要(截断120字) */
    private String summary;
    private String category;
    /** 首图 */
    private String cover;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private LocalDateTime createTime;
    /** 审核状态(我的帖子/管理端可见) */
    private Integer status;
    private String rejectReason;
    private AuthorVO author;
}
