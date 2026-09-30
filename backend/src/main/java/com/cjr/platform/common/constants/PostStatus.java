package com.cjr.platform.common.constants;

/**
 * 帖子/经验审核状态
 */
public interface PostStatus {

    /** 待审核 */
    int PENDING = 0;
    /** 已通过 */
    int APPROVED = 1;
    /** 已驳回 */
    int REJECTED = 2;
}
