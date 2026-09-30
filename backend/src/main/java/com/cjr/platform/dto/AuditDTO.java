package com.cjr.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审核入参(帖子/经验/求助通用): status=1通过 2/3驳回
 */
@Data
public class AuditDTO {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Size(max = 255, message = "驳回原因最长255字")
    private String rejectReason;
}
