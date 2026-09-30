package com.cjr.platform.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 管理端统计专用 SQL(手写 @Select 注意逻辑删除字段需手动过滤)
 */
public interface StatsMapper {

    /** 近 N 天每日新增用户/帖子/捐助金额(DATE_FORMAT 转字符串; GROUP BY 与 SELECT 表达式保持一致以兼容 only_full_group_by) */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS cnt FROM `user` " +
            "WHERE deleted = 0 AND create_time >= #{start} GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> dailyNewUsers(@Param("start") LocalDate start);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS cnt FROM post " +
            "WHERE deleted = 0 AND status = 1 AND create_time >= #{start} GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> dailyNewPosts(@Param("start") LocalDate start);

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COALESCE(SUM(amount), 0) AS amount FROM donation " +
            "WHERE create_time >= #{start} GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> dailyDonationAmount(@Param("start") LocalDate start);

    /** 帖子分类分布 */
    @Select("SELECT category AS name, COUNT(*) AS value FROM post " +
            "WHERE deleted = 0 AND status = 1 GROUP BY category")
    List<Map<String, Object>> postCategoryStats();
}
