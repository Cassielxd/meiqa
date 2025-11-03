-- =====================================================
-- 添加 request_url 和 request_url_updated_time 字段
-- 用于存储游客当前请求的完整URL（包含参数）
-- =====================================================

-- 1. 添加 request_url 字段（当前请求的完整URL）
ALTER TABLE `eb_chat_user`
ADD COLUMN `request_url` VARCHAR(1000) NULL DEFAULT NULL COMMENT '当前请求的完整URL（包含参数）' AFTER `referer_updated_time`;

-- 2. 添加 request_url_updated_time 字段（Request URL 更新时间）
ALTER TABLE `eb_chat_user`
ADD COLUMN `request_url_updated_time` INT(11) NULL DEFAULT NULL COMMENT 'Request URL 更新时间（Unix时间戳）' AFTER `request_url`;

-- 3. 添加索引（可选，用于查询优化）
ALTER TABLE `eb_chat_user`
ADD INDEX `idx_request_url_updated_time` (`request_url_updated_time`);

-- 4. 查看表结构
DESC `eb_chat_user`;

-- =====================================================
-- 说明：
-- 1. referer 字段：存储 HTTP Referer 头（来源页面）
-- 2. request_url 字段：存储当前请求的完整 URL（包含参数）
-- 3. 两个字段各自独立，分别记录不同的信息
-- =====================================================

