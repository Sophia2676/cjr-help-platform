package com.cjr.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 求助信息(含进度)
 */
@Data
public class HelpRequestVO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal raisedAmount;
    private Integer donateCount;
    /** 筹款进度 0-100 */
    private Integer progress;
    private List<String> images;
    /** 联系电话(仅登录用户可见) */
    private String contactPhone;
    /** 求助人账号手机号(仅社区工作者/管理员可见) */
    private String userPhone;
    private Integer status;
    private String rejectReason;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private AuthorVO user;
    /** 最近捐助记录(详情页展示) */
    private List<DonationVO> recentDonations;
}
