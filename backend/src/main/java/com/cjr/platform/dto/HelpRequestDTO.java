package com.cjr.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HelpRequestDTO {

    @NotBlank(message = "求助标题不能为空")
    @Size(max = 50, message = "标题最长50字")
    private String title;

    @NotBlank(message = "请填写详细情况说明")
    @Size(max = 5000, message = "情况说明最长5000字")
    private String description;

    @NotNull(message = "请填写目标金额")
    @DecimalMin(value = "0.01", message = "目标金额必须大于0")
    @DecimalMax(value = "999999.99", message = "目标金额过大")
    private BigDecimal targetAmount;

    @Size(max = 20, message = "联系电话格式不正确")
    private String contactPhone;

    /** 证明材料图片路径列表 */
    private List<String> images;
}
