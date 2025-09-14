-- =============================================
-- CRMChat 租户化数据库迁移脚本 (最终版)
-- 创建时间: 2025-09-14
-- 用途: 基于APPID实现简易租户化改造
-- 特点: 安全的一次性执行脚本，自动检查字段和索引是否存在
-- =============================================

SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- =============================================
-- 1. 创建租户表
-- =============================================
CREATE TABLE IF NOT EXISTS `tenants` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `appid` varchar(32) NOT NULL DEFAULT '' COMMENT '应用ID，对应application表',
    `tenant_name` varchar(100) NOT NULL DEFAULT '' COMMENT '租户名称',
    `tenant_code` varchar(50) NOT NULL DEFAULT '' COMMENT '租户编码',
    `domain` varchar(100) DEFAULT '' COMMENT '租户域名',
    `logo` varchar(255) DEFAULT '' COMMENT '租户logo',
    `contact_name` varchar(50) DEFAULT '' COMMENT '联系人姓名',
    `contact_phone` varchar(20) DEFAULT '' COMMENT '联系人电话',
    `contact_email` varchar(100) DEFAULT '' COMMENT '联系人邮箱',
    `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1=启用，0=禁用',
    `max_users` int(11) DEFAULT '1000' COMMENT '最大用户数限制',
    `max_services` int(11) DEFAULT '10' COMMENT '最大客服数限制',
    `expire_time` int(11) DEFAULT '0' COMMENT '到期时间',
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_appid` (`appid`),
    UNIQUE KEY `unique_tenant_code` (`tenant_code`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- =============================================
-- 2. 安全添加appid字段的通用方法
-- =============================================

-- 2.1 为聊天记录表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_record' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_service_record` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_service_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.2 为对话记录表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_dialogue_record' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_service_dialogue_record` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_service_dialogue_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.3 为客服反馈表添加appid字段（该表可能已有此字段）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_feedback' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_service_feedback` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_service_feedback表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.4 为客服话术表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_speechcraft' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_service_speechcraft` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_service_speechcraft表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.5 为客服分组表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_group' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_service_group` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_service_group表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.6 为用户分组表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_group' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_user_group` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_user_group表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.7 为用户标签表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_user_label` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_user_label表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.8 为用户标签关联表添加appid字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label_assist' 
    AND COLUMN_NAME = 'appid') = 0,
    'ALTER TABLE `eb_chat_user_label_assist` ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT "" COMMENT "APPID" AFTER `id`;',
    'SELECT "appid字段已存在于eb_chat_user_label_assist表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 3. 安全添加索引的通用方法
-- =============================================

-- 3.1 聊天记录表索引
-- 基础appid索引
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_record' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_service_record` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_service_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- appid+user_id复合索引
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_record' 
    AND INDEX_NAME = 'idx_appid_user_id') = 0,
    'ALTER TABLE `eb_chat_service_record` ADD INDEX `idx_appid_user_id` (`appid`, `user_id`);',
    'SELECT "idx_appid_user_id索引已存在于eb_chat_service_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- appid+to_user_id复合索引
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_record' 
    AND INDEX_NAME = 'idx_appid_to_user_id') = 0,
    'ALTER TABLE `eb_chat_service_record` ADD INDEX `idx_appid_to_user_id` (`appid`, `to_user_id`);',
    'SELECT "idx_appid_to_user_id索引已存在于eb_chat_service_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.2 对话记录表索引
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_dialogue_record' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_service_dialogue_record` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_service_dialogue_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_dialogue_record' 
    AND INDEX_NAME = 'idx_appid_user_id') = 0,
    'ALTER TABLE `eb_chat_service_dialogue_record` ADD INDEX `idx_appid_user_id` (`appid`, `user_id`);',
    'SELECT "idx_appid_user_id索引已存在于eb_chat_service_dialogue_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_dialogue_record' 
    AND INDEX_NAME = 'idx_appid_to_user_id') = 0,
    'ALTER TABLE `eb_chat_service_dialogue_record` ADD INDEX `idx_appid_to_user_id` (`appid`, `to_user_id`);',
    'SELECT "idx_appid_to_user_id索引已存在于eb_chat_service_dialogue_record表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3.3 其他表的基础索引
-- 反馈表
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_feedback' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_service_feedback` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_service_feedback表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 话术表
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_speechcraft' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_service_speechcraft` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_service_speechcraft表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_speechcraft' 
    AND INDEX_NAME = 'idx_appid_kefu_id') = 0,
    'ALTER TABLE `eb_chat_service_speechcraft` ADD INDEX `idx_appid_kefu_id` (`appid`, `kefu_id`);',
    'SELECT "idx_appid_kefu_id索引已存在于eb_chat_service_speechcraft表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 客服分组表
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service_group' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_service_group` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_service_group表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 用户分组表
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_group' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_user_group` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_user_group表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 用户标签表
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_user_label` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_user_label表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label' 
    AND INDEX_NAME = 'idx_appid_cate_id') = 0,
    'ALTER TABLE `eb_chat_user_label` ADD INDEX `idx_appid_cate_id` (`appid`, `cate_id`);',
    'SELECT "idx_appid_cate_id索引已存在于eb_chat_user_label表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 用户标签关联表
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label_assist' 
    AND INDEX_NAME = 'idx_appid') = 0,
    'ALTER TABLE `eb_chat_user_label_assist` ADD INDEX `idx_appid` (`appid`);',
    'SELECT "idx_appid索引已存在于eb_chat_user_label_assist表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label_assist' 
    AND INDEX_NAME = 'idx_appid_user_id') = 0,
    'ALTER TABLE `eb_chat_user_label_assist` ADD INDEX `idx_appid_user_id` (`appid`, `user_id`);',
    'SELECT "idx_appid_user_id索引已存在于eb_chat_user_label_assist表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user_label_assist' 
    AND INDEX_NAME = 'idx_appid_label_id') = 0,
    'ALTER TABLE `eb_chat_user_label_assist` ADD INDEX `idx_appid_label_id` (`appid`, `label_id`);',
    'SELECT "idx_appid_label_id索引已存在于eb_chat_user_label_assist表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 4. 为现有表添加复合索引优化查询性能
-- =============================================

-- chat_user表优化（已有appid字段）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user' 
    AND INDEX_NAME = 'idx_appid_group_id') = 0,
    'ALTER TABLE `eb_chat_user` ADD INDEX `idx_appid_group_id` (`appid`, `group_id`);',
    'SELECT "idx_appid_group_id索引已存在于eb_chat_user表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user' 
    AND INDEX_NAME = 'idx_appid_type') = 0,
    'ALTER TABLE `eb_chat_user` ADD INDEX `idx_appid_type` (`appid`, `type`);',
    'SELECT "idx_appid_type索引已存在于eb_chat_user表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_user' 
    AND INDEX_NAME = 'idx_appid_online') = 0,
    'ALTER TABLE `eb_chat_user` ADD INDEX `idx_appid_online` (`appid`, `online`);',
    'SELECT "idx_appid_online索引已存在于eb_chat_user表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- chat_service表优化（已有appid字段）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service' 
    AND INDEX_NAME = 'idx_appid_status') = 0,
    'ALTER TABLE `eb_chat_service` ADD INDEX `idx_appid_status` (`appid`, `status`);',
    'SELECT "idx_appid_status索引已存在于eb_chat_service表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_service' 
    AND INDEX_NAME = 'idx_appid_online') = 0,
    'ALTER TABLE `eb_chat_service` ADD INDEX `idx_appid_online` (`appid`, `online`);',
    'SELECT "idx_appid_online索引已存在于eb_chat_service表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- chat_auto_reply表优化（已有appid字段）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'eb_chat_auto_reply' 
    AND INDEX_NAME = 'idx_appid_user_id') = 0,
    'ALTER TABLE `eb_chat_auto_reply` ADD INDEX `idx_appid_user_id` (`appid`, `user_id`);',
    'SELECT "idx_appid_user_id索引已存在于eb_chat_auto_reply表中" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================
-- 5. 创建默认租户数据（基于现有application表）
-- =============================================
INSERT INTO `tenants` (`appid`, `tenant_name`, `tenant_code`, `status`, `created_at`)
SELECT 
    `appid`,
    COALESCE(`name`, CONCAT('租户_', `appid`)) as tenant_name,
    `appid` as tenant_code,
    IF(`is_delete` = 0, 1, 0) as status,
    NOW() as created_at
FROM `eb_application` 
WHERE `appid` != '' 
ON DUPLICATE KEY UPDATE 
    `tenant_name` = VALUES(`tenant_name`),
    `status` = VALUES(`status`),
    `updated_at` = NOW();

-- =============================================
-- 6. 更新现有数据的appid字段
-- =============================================

-- 更新聊天记录的appid（从chat_user表获取）
UPDATE `eb_chat_service_record` csr 
LEFT JOIN `eb_chat_user` cu ON cu.id = csr.user_id 
SET csr.appid = cu.appid 
WHERE csr.appid = '' AND cu.appid != '';

-- 更新对话记录的appid（从chat_user表获取）
UPDATE `eb_chat_service_dialogue_record` csdr 
LEFT JOIN `eb_chat_user` cu ON cu.id = csdr.user_id 
SET csdr.appid = cu.appid 
WHERE csdr.appid = '' AND cu.appid != '';

-- 更新话术表的appid（从chat_service表获取）
UPDATE `eb_chat_service_speechcraft` css 
LEFT JOIN `eb_chat_service` cs ON cs.id = css.kefu_id 
SET css.appid = cs.appid 
WHERE css.appid = '' AND cs.appid != '';

-- 更新用户标签关联的appid（从chat_user表获取）
UPDATE `eb_chat_user_label_assist` cula 
LEFT JOIN `eb_chat_user` cu ON cu.id = cula.user_id 
SET cula.appid = cu.appid 
WHERE cula.appid = '' AND cu.appid != '';

-- 如果存在孤立的分组和标签数据，可以从第一个available的application获取默认appid
UPDATE `eb_chat_service_group` 
SET `appid` = (SELECT `appid` FROM `eb_application` WHERE `is_delete` = 0 LIMIT 1)
WHERE `appid` = '' AND EXISTS (SELECT 1 FROM `eb_application` WHERE `is_delete` = 0);

UPDATE `eb_chat_user_group` 
SET `appid` = (SELECT `appid` FROM `eb_application` WHERE `is_delete` = 0 LIMIT 1)
WHERE `appid` = '' AND EXISTS (SELECT 1 FROM `eb_application` WHERE `is_delete` = 0);

UPDATE `eb_chat_user_label` 
SET `appid` = (SELECT `appid` FROM `eb_application` WHERE `is_delete` = 0 LIMIT 1)
WHERE `appid` = '' AND EXISTS (SELECT 1 FROM `eb_application` WHERE `is_delete` = 0);

-- =============================================
-- 7. 验证脚本：检查租户化改造结果
-- =============================================

-- 检查租户表
SELECT '=== 租户表检查 ===' as section;
SELECT '租户表记录数:' as info, COUNT(*) as count FROM `tenants`;
SELECT '租户详情:' as info, `appid`, `tenant_name`, `status` FROM `tenants` LIMIT 10;

-- 检查各表appid字段覆盖情况
SELECT '=== appid字段覆盖率检查 ===' as section;

SELECT '聊天记录appid覆盖率:' as info, 
       CONCAT(ROUND(SUM(CASE WHEN appid != '' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2), '%') as coverage,
       COUNT(*) as total_records,
       SUM(CASE WHEN appid != '' THEN 1 ELSE 0 END) as records_with_appid
FROM `eb_chat_service_record`;

SELECT '对话记录appid覆盖率:' as info, 
       CONCAT(ROUND(SUM(CASE WHEN appid != '' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2), '%') as coverage,
       COUNT(*) as total_records,
       SUM(CASE WHEN appid != '' THEN 1 ELSE 0 END) as records_with_appid
FROM `eb_chat_service_dialogue_record`;

SELECT '话术appid覆盖率:' as info, 
       CONCAT(ROUND(SUM(CASE WHEN appid != '' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2), '%') as coverage,
       COUNT(*) as total_records,
       SUM(CASE WHEN appid != '' THEN 1 ELSE 0 END) as records_with_appid
FROM `eb_chat_service_speechcraft`;

-- 检查索引创建情况
SELECT '=== 租户索引检查 ===' as section;
SELECT 
    TABLE_NAME as '表名',
    INDEX_NAME as '索引名',
    COLUMN_NAME as '字段名'
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND INDEX_NAME LIKE '%appid%'
    AND TABLE_NAME LIKE 'eb_chat_%'
ORDER BY TABLE_NAME, INDEX_NAME;

-- 检查新增字段
SELECT '=== 新增appid字段检查 ===' as section;
SELECT 
    TABLE_NAME as '表名',
    COLUMN_NAME as '字段名',
    COLUMN_TYPE as '字段类型',
    IS_NULLABLE as '是否可空',
    COLUMN_DEFAULT as '默认值',
    COLUMN_COMMENT as '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() 
    AND COLUMN_NAME = 'appid'
    AND TABLE_NAME LIKE 'eb_chat_%'
ORDER BY TABLE_NAME;

-- 恢复SQL模式
SET SQL_MODE=@OLD_SQL_MODE;

-- =============================================
-- 脚本执行完成
-- =============================================
SELECT '=== 执行完成 ===' as section;
SELECT '🎉 租户化迁移脚本执行完成！' as message, NOW() as completion_time;

-- =============================================
-- 回滚脚本（仅在需要时执行，请小心使用）
-- =============================================
/*
-- 警告：以下回滚操作将删除租户表和所有appid字段，请谨慎执行！

-- 删除租户表
DROP TABLE IF EXISTS `tenants`;

-- 删除新增的appid字段（请根据实际情况选择性执行）
ALTER TABLE `eb_chat_service_record` DROP COLUMN IF EXISTS `appid`;
ALTER TABLE `eb_chat_service_dialogue_record` DROP COLUMN IF EXISTS `appid`;
ALTER TABLE `eb_chat_service_speechcraft` DROP COLUMN IF EXISTS `appid`;
ALTER TABLE `eb_chat_service_group` DROP COLUMN IF EXISTS `appid`;
ALTER TABLE `eb_chat_user_group` DROP COLUMN IF EXISTS `appid`;
ALTER TABLE `eb_chat_user_label` DROP COLUMN IF EXISTS `appid`;
ALTER TABLE `eb_chat_user_label_assist` DROP COLUMN IF EXISTS `appid`;

-- 注意：eb_chat_service_feedback表的appid字段可能是原本就存在的，删除前请确认
-- ALTER TABLE `eb_chat_service_feedback` DROP COLUMN IF EXISTS `appid`;
*/