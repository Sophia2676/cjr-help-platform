package com.cjr.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 近 N 天趋势(ECharts 折线图数据)
 */
@Data
public class TrendVO {

    private List<String> dates;
    private List<Long> userCounts;
    private List<Long> postCounts;
    private List<BigDecimal> donationAmounts;
}
