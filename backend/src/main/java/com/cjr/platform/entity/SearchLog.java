package com.cjr.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户搜索记录表（智能推荐依据：某关键词搜索次数 > 20 次即推送相关帖子）
 */
@Data
@TableName("search_log")
public class SearchLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String keyword;

    /** 累计搜索次数 */
    private Integer count;

    private LocalDateTime lastTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
