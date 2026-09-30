package com.cjr.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端待审核内容混合列表项
 */
@Data
public class PendingItemVO {

    /** POST帖子/EXPERIENCE经验/HELP求助 */
    private String type;
    private Long id;
    private String title;
    /** 提交人昵称 */
    private String nickname;
    private LocalDateTime createTime;
}
