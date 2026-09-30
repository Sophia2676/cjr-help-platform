package com.cjr.platform.dto;

import lombok.Data;

/**
 * 基础分页入参(GET 请求 query 绑定)
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 搜索关键字(标题模糊匹配) */
    private String keyword;
}
