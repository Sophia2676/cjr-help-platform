package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 政策资讯详情
 */
@Data
public class PolicyDetailVO {

    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private String source;
    private LocalDateTime publishTime;
    private Integer viewCount;
}
