-- =============================================
-- 数据库迁移脚本：为 eb_chat_user 表添加 referer 字段
-- 创建时间：2025-10-31
-- 用途：记录游客访问来源（HTTP Referer）
-- =============================================

-- 添加 referer 字段（来源页面 URL）
ALTER TABLE `eb_chat_user` 
ADD COLUMN `referer` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '来源页面（HTTP Referer）' AFTER `geo_updated_time`;

-- 添加 referer_updated_time 字段（Referer 更新时间）
ALTER TABLE `eb_chat_user` 
ADD COLUMN `referer_updated_time` INT NOT NULL DEFAULT 0 COMMENT 'Referer 更新时间（Unix时间戳）' AFTER `referer`;

-- 为 referer 字段添加索引（方便按来源查询）
ALTER TABLE `eb_chat_user` 
ADD INDEX `idx_referer` (`referer`(255));

-- 为 referer_updated_time 字段添加索引（方便按更新时间查询）
ALTER TABLE `eb_chat_user` 
ADD INDEX `idx_referer_updated_time` (`referer_updated_time`);

-- 查看表结构确认
SHOW COLUMNS FROM `eb_chat_user` LIKE 'referer%';

