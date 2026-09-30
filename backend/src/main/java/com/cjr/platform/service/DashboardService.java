package com.cjr.platform.service;

import com.cjr.platform.vo.DashboardStatsVO;
import com.cjr.platform.vo.NameValueVO;
import com.cjr.platform.vo.PendingItemVO;
import com.cjr.platform.vo.TrendVO;

import java.util.List;

public interface DashboardService {

    DashboardStatsVO stats();

    /** 近 N 天趋势(折线图) */
    TrendVO trend(int days);

    /** 帖子分类分布(饼图) */
    List<NameValueVO> category();

    /** 最新待审核内容(混合列表) */
    List<PendingItemVO> latest(int limit);
}
