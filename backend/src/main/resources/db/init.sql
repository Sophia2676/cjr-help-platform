-- =====================================================
-- 残疾人互助交流平台 数据库初始化脚本
-- MySQL 8.0 / 数据库: cjr_platform / 字符集: utf8mb4
-- 仅建库建表；种子数据由后端 DataInitializer 幂等写入
-- =====================================================
CREATE DATABASE IF NOT EXISTS cjr_platform
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE cjr_platform;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`         VARCHAR(50)  NOT NULL                COMMENT '登录名',
  `password`         VARCHAR(100) NOT NULL                COMMENT 'BCrypt密码哈希',
  `nickname`         VARCHAR(50)  NOT NULL                COMMENT '昵称',
  `real_name`        VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
  `avatar`           VARCHAR(255) DEFAULT NULL            COMMENT '头像路径(/uploads/...)',
  `phone`            VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `email`            VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `gender`           TINYINT      NOT NULL DEFAULT 0      COMMENT '性别 0未知1男2女',
  `disability_type`  VARCHAR(50)  DEFAULT NULL            COMMENT '残疾类别(视力/听力/言语/肢体/智力/精神/多重)',
  `disability_level` TINYINT      DEFAULT NULL            COMMENT '残疾等级1-4级',
  `role`             VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
  `status`           TINYINT      NOT NULL DEFAULT 1      COMMENT '账号状态 1正常0禁用',
  `bio`              VARCHAR(500) DEFAULT NULL            COMMENT '个人简介',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role_status` (`role`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- 2. 互助帖子表
CREATE TABLE IF NOT EXISTS `post` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '作者用户ID(逻辑外键->user.id)',
  `title`         VARCHAR(100) NOT NULL                   COMMENT '标题(1-50字)',
  `content`       TEXT         NOT NULL                   COMMENT '正文内容',
  `category`      VARCHAR(30)  NOT NULL DEFAULT '其他'    COMMENT '分类 求医问药/生活求助/出行交流/心理互助/求职就业/其他',
  `images`        VARCHAR(1000) DEFAULT NULL              COMMENT '图片路径列表 英文逗号分隔',
  `view_count`    INT          NOT NULL DEFAULT 0         COMMENT '浏览量',
  `like_count`    INT          NOT NULL DEFAULT 0         COMMENT '点赞数(冗余计数)',
  `comment_count` INT          NOT NULL DEFAULT 0         COMMENT '评论数(冗余计数)',
  `status`        TINYINT      NOT NULL DEFAULT 0         COMMENT '审核状态 0待审核1已通过2已驳回',
  `reject_reason` VARCHAR(255) DEFAULT NULL               COMMENT '驳回原因',
  `is_top`        TINYINT      NOT NULL DEFAULT 0         COMMENT '是否置顶 0否1是',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0         COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_time` (`status`,`create_time`),
  KEY `idx_category` (`category`),
  KEY `idx_like_count` (`like_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='互助帖子表';

-- 3. 帖子评论表（两级：一级评论+回复）
CREATE TABLE IF NOT EXISTS `comment` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id`       BIGINT UNSIGNED NOT NULL                COMMENT '帖子ID(逻辑外键->post.id)',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '评论人ID(逻辑外键->user.id)',
  `parent_id`     BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '父评论ID 0=一级评论 否则=被回复评论ID',
  `reply_user_id` BIGINT UNSIGNED DEFAULT NULL            COMMENT '被回复人ID',
  `content`       VARCHAR(500) NOT NULL                   COMMENT '评论内容(1-500字)',
  `status`        TINYINT      NOT NULL DEFAULT 1         COMMENT '状态 1正常0已删除',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子评论表';

-- 4. 帖子点赞表
CREATE TABLE IF NOT EXISTS `post_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id`     BIGINT UNSIGNED NOT NULL COMMENT '帖子ID(逻辑外键->post.id)',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '点赞人ID(逻辑外键->user.id)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞表';

-- 5. 康复养护经验表
CREATE TABLE IF NOT EXISTS `experience` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '分享人ID(逻辑外键->user.id)',
  `title`         VARCHAR(100) NOT NULL                   COMMENT '标题(1-50字)',
  `content`       TEXT         NOT NULL                   COMMENT '正文',
  `category`      VARCHAR(30)  NOT NULL DEFAULT '康复训练' COMMENT '分类 康复训练/日常护理/心理疏导/辅助器具/饮食营养/其他',
  `images`        VARCHAR(1000) DEFAULT NULL              COMMENT '图片路径列表 英文逗号分隔',
  `view_count`    INT          NOT NULL DEFAULT 0         COMMENT '浏览量',
  `status`        TINYINT      NOT NULL DEFAULT 0         COMMENT '审核状态 0待审核1已通过2已驳回',
  `reject_reason` VARCHAR(255) DEFAULT NULL               COMMENT '驳回原因',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0         COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_time` (`status`,`create_time`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='康复养护经验表';

-- 6. 捐助求助表（一对一爱心捐助）
CREATE TABLE IF NOT EXISTS `help_request` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT UNSIGNED NOT NULL                COMMENT '求助人ID(逻辑外键->user.id)',
  `title`          VARCHAR(100) NOT NULL                   COMMENT '求助标题',
  `description`    TEXT         NOT NULL                   COMMENT '详细情况说明',
  `target_amount`  DECIMAL(10,2) NOT NULL                  COMMENT '目标金额(元) >0',
  `raised_amount`  DECIMAL(10,2) NOT NULL DEFAULT 0.00     COMMENT '已筹金额(元)',
  `donate_count`   INT          NOT NULL DEFAULT 0         COMMENT '捐助人次',
  `images`         VARCHAR(1000) DEFAULT NULL              COMMENT '证明材料图片 英文逗号分隔',
  `contact_phone`  VARCHAR(20)  DEFAULT NULL               COMMENT '联系电话(仅展示给登录用户)',
  `status`         TINYINT      NOT NULL DEFAULT 0         COMMENT '状态 0待审核1募捐中2已完成3已驳回',
  `reject_reason`  VARCHAR(255) DEFAULT NULL               COMMENT '驳回原因',
  `complete_time`  DATETIME     DEFAULT NULL               COMMENT '完成时间',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      NOT NULL DEFAULT 0         COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_time` (`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='捐助求助表';

-- 7. 捐助记录表
CREATE TABLE IF NOT EXISTS `donation` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `help_request_id` BIGINT UNSIGNED NOT NULL                COMMENT '求助ID(逻辑外键->help_request.id)',
  `donor_id`        BIGINT UNSIGNED NOT NULL                COMMENT '捐助人ID(逻辑外键->user.id)',
  `amount`          DECIMAL(10,2) NOT NULL                  COMMENT '捐助金额(元) >0',
  `message`         VARCHAR(255) DEFAULT NULL               COMMENT '爱心留言',
  `is_anonymous`    TINYINT      NOT NULL DEFAULT 0         COMMENT '是否匿名 0否1是(匿名则对外显示爱心人士)',
  `status`          TINYINT      NOT NULL DEFAULT 1         COMMENT '状态 1已捐助2受助人已确认',
  `confirm_time`    DATETIME     DEFAULT NULL               COMMENT '受助人确认时间',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '捐助时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`help_request_id`),
  KEY `idx_donor_id` (`donor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='捐助记录表';

-- 8. 残联政策资讯表
CREATE TABLE IF NOT EXISTS `policy` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(100) NOT NULL                    COMMENT '标题',
  `summary`      VARCHAR(255) DEFAULT NULL                COMMENT '摘要(列表页展示)',
  `content`      TEXT         NOT NULL                    COMMENT '正文',
  `cover_image`  VARCHAR(255) DEFAULT NULL                COMMENT '封面图路径',
  `source`       VARCHAR(100) DEFAULT NULL                COMMENT '来源(如:市残联)',
  `publish_time` DATETIME     DEFAULT NULL                COMMENT '发布时间(发布时写入now)',
  `view_count`   INT          NOT NULL DEFAULT 0          COMMENT '浏览量',
  `status`       TINYINT      NOT NULL DEFAULT 1          COMMENT '状态 1已发布0下架',
  `create_by`    BIGINT UNSIGNED DEFAULT NULL             COMMENT '发布管理员ID(逻辑外键->user.id)',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0          COMMENT '逻辑删除 0否1是',
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`,`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='残联政策资讯表';

-- 9. 站内消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `receiver_id` BIGINT UNSIGNED NOT NULL                COMMENT '接收人ID(逻辑外键->user.id)',
  `sender_id`   BIGINT UNSIGNED DEFAULT NULL            COMMENT '发送人ID NULL=系统',
  `type`        VARCHAR(30)  NOT NULL                   COMMENT '类型 AUDIT审核结果/DONATION捐助动态/SYSTEM系统通知',
  `title`       VARCHAR(100) NOT NULL                   COMMENT '标题',
  `content`     VARCHAR(500) NOT NULL                   COMMENT '内容',
  `related_id`  BIGINT UNSIGNED DEFAULT NULL            COMMENT '关联业务ID(帖子/求助ID,供前端跳转)',
  `is_read`     TINYINT      NOT NULL DEFAULT 0         COMMENT '是否已读 0否1是',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_read` (`receiver_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内消息表';

-- 10. 操作日志表(AOP切面写入)
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT UNSIGNED DEFAULT NULL         COMMENT '操作人ID',
  `username`       VARCHAR(50)  DEFAULT NULL            COMMENT '操作人用户名',
  `operation`      VARCHAR(100) NOT NULL                COMMENT '操作描述(如:发布帖子/审核帖子)',
  `method`         VARCHAR(200) DEFAULT NULL            COMMENT '执行方法 类名.方法名',
  `request_method` VARCHAR(10)  DEFAULT NULL            COMMENT 'HTTP方法 GET/POST/PUT/DELETE',
  `request_uri`    VARCHAR(255) DEFAULT NULL            COMMENT '请求URI',
  `request_params` TEXT                                  COMMENT '请求参数JSON(已脱敏截断,最长2000字符)',
  `ip`             VARCHAR(50)  DEFAULT NULL            COMMENT '客户端IP',
  `status`         TINYINT      NOT NULL DEFAULT 1      COMMENT '1成功0异常',
  `error_msg`      VARCHAR(500) DEFAULT NULL            COMMENT '异常信息',
  `cost_time`      BIGINT       DEFAULT NULL            COMMENT '耗时(毫秒)',
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_operation` (`operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- =====================================================
-- 2026-09-30 新增：智能推荐 + 微信登录 相关表结构
-- =====================================================

-- 用户搜索记录表（智能推荐：某关键词搜索次数 > 20 则推送相关帖子）
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
