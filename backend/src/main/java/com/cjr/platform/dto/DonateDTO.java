package com.cjr.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonateDTO {

    @NotNull(message = "请填写捐助金额")
    @DecimalMin(value = "0.01", message = "捐助金额必须大于0")
    private BigDecimal amount;

    @Size(max = 255, message = "爱心留言最长255字")
    private String message;

    /** 是否匿名 0否1是 */
    private Integer isAnonymous = 0;
}
