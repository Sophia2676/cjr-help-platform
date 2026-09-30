package com.cjr.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用状态修改入参(用户禁用/政策上下架/帖子置顶等)
 */
@Data
public class StatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;

    /** 置顶时传 1/0 */
    private Integer isTop;
}
