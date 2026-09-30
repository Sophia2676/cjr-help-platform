package com.cjr.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端数据概览
 */
@Data
public class DashboardStatsVO {

    private Long userCount;
    private Long postCount;
    private Long experienceCount;
    private Long helpCount;
    /** 累计捐助金额(元) */
    private BigDecimal donationTotal;
    /** 累计捐助人次 */
    private Long donateCount;
    /** 待审核数 */
    private Long pendingPostCount;
    private Long pendingExperienceCount;
    private Long pendingHelpCount;
    /** 今日新增 */
    private Long todayNewUser;
    private Long todayNewPost;
    private BigDecimal todayDonationAmount;
}
