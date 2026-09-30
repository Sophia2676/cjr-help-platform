package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息
 */
@Data
public class MessageVO {

    private Long id;
    private String type;
    private String title;
    private String content;
    /** 关联业务ID(帖子/求助ID,供前端跳转) */
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createTime;
}
