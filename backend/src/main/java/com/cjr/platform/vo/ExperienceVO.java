package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 康复经验列表项
 */
@Data
public class ExperienceVO {

    private Long id;
    private String title;
    private String summary;
    private String category;
    private String cover;
    private Integer viewCount;
    private LocalDateTime createTime;
    private Integer status;
    private String rejectReason;
    private AuthorVO author;
}
