package com.cjr.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐助记录(匿名捐助对外显示"爱心人士")
 */
@Data
public class DonationVO {

    private Long id;
    private Long helpRequestId;
    /** 求助标题(关联查询) */
    private String helpTitle;
    private BigDecimal amount;
    private String message;
    private Integer isAnonymous;
    /** 1已捐助2受助人已确认 */
    private Integer status;
    private LocalDateTime confirmTime;
    private LocalDateTime createTime;
    /** 捐助人昵称(匿名时已处理为"爱心人士") */
    private String donorNickname;
    private String donorAvatar;
}
