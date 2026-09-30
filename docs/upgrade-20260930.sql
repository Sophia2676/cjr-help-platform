-- =====================================================
-- 升级脚本（已部署数据库执行此文件即可，无需重建库）
-- 新增功能：智能推荐(搜索记录) / 微信登录(openid) / 社区工作者角色
-- 用法：mysql -uroot -p < docs/upgrade-20260930.sql

USE cjr_platform;
-- =====================================================

-- 1. 用户表增加微信 openid
ALTER TABLE `user` ADD COLUMN `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信openid' AFTER `phone`;

-- 2. 搜索记录表
CREATE TABLE IF NOT EXISTS `search_log` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`      BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
  `keyword`      VARCHAR(100) NOT NULL                   COMMENT '搜索关键词',
  `count`        INT          NOT NULL DEFAULT 1         COMMENT '累计搜索次数',
  `last_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近搜索时间',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_keyword` (`user_id`,`keyword`),
  KEY `idx_user_count` (`user_id`,`count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户搜索记录表';

-- 3. openid 唯一索引（可选，便于微信登录查重）
ALTER TABLE `user` ADD UNIQUE KEY `uk_openid` (`openid`);
