package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 康复经验详情
 */
@Data
public class ExperienceDetailVO {

    private Long id;
    private String title;
    private String content;
    private String category;
    private List<String> images;
    private Integer viewCount;
    private LocalDateTime createTime;
    private AuthorVO author;
}
