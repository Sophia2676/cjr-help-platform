package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 政策资讯列表项
 */
@Data
public class PolicyVO {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private String source;
    private LocalDateTime publishTime;
    private Integer viewCount;
}
