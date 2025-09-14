-- =============================================
-- CRMChat 租户化数据库迁移脚本 (精简版)
-- 创建时间: 2025-09-14
-- =============================================

-- 1. 创建租户表
CREATE TABLE IF NOT EXISTS `eb_tenants` (
    `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '租户ID',
    `appid` varchar(32) NOT NULL DEFAULT '' COMMENT '应用ID，对应application表',
    `tenant_name` varchar(100) NOT NULL DEFAULT '' COMMENT '租户名称',
    `tenant_code` varchar(50) NOT NULL DEFAULT '' COMMENT '租户编码',
    `account` varchar(32) NOT NULL DEFAULT '' COMMENT '租户管理员账号',
    `pwd` varchar(100) NOT NULL DEFAULT '' COMMENT '租户管理员密码',
    `domain` varchar(100) DEFAULT '' COMMENT '租户域名',
    `logo` varchar(255) DEFAULT '' COMMENT '租户logo',
    `contact_name` varchar(50) DEFAULT '' COMMENT '联系人姓名',
    `contact_phone` varchar(20) DEFAULT '' COMMENT '联系人电话',
    `contact_email` varchar(100) DEFAULT '' COMMENT '联系人邮箱',
    `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态：0=待审核，1=已批准，2=已拒绝，3=已禁用',
    `max_users` int(11) DEFAULT '1000' COMMENT '最大用户数限制',
    `max_services` int(11) DEFAULT '10' COMMENT '最大客服数限制',
    `expire_at` timestamp NULL DEFAULT NULL COMMENT '到期时间',
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_appid` (`appid`),
    UNIQUE KEY `unique_tenant_code` (`tenant_code`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 2. 为eb_chat_user_label_assist表添加appid字段
ALTER TABLE `eb_chat_user_label_assist` 
ADD COLUMN `appid` varchar(32) NOT NULL DEFAULT '' COMMENT 'APPID' AFTER `id`;

-- 3. 为eb_chat_user_label_assist表添加索引
ALTER TABLE `eb_chat_user_label_assist` ADD INDEX `idx_appid` (`appid`);
ALTER TABLE `eb_chat_user_label_assist` ADD INDEX `idx_appid_user_id` (`appid`, `user_id`);
ALTER TABLE `eb_chat_user_label_assist` ADD INDEX `idx_appid_label_id` (`appid`, `label_id`);

-- 4. 执行完成
SELECT '🎉 租户化迁移脚本执行完成！' as message;