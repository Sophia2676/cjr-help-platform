package com.cjr.platform.common.constants;

/**
 * 捐助求助状态
 */
public interface HelpStatus {

    /** 待审核 */
    int PENDING = 0;
    /** 募捐中 */
    int RAISING = 1;
    /** 已完成 */
    int COMPLETED = 2;
    /** 已驳回 */
    int REJECTED = 3;
}
