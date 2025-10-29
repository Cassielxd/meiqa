-- ============================================
-- 客服互聊功能 - 数据迁移脚本
-- ============================================
-- 目的：为现有客服创建 chat_user 记录，支持客服之间互相聊天
-- 执行时间：预计 < 1分钟（取决于客服数量）
-- 影响范围：eb_chat_user 表、eb_chat_service 表
-- ============================================

-- 步骤1：检查当前状态
-- 查看有多少客服没有关联的 chat_user 记录
SELECT 
    COUNT(*) AS total_kefu,
    SUM(CASE WHEN user_id IS NULL THEN 1 ELSE 0 END) AS kefu_without_user,
    SUM(CASE WHEN user_id IS NOT NULL THEN 1 ELSE 0 END) AS kefu_with_user
FROM eb_chat_service;

-- 查看有多少 chat_user 记录已经标记为客服
SELECT 
    COUNT(*) AS total_users,
    SUM(CASE WHEN is_kefu = 1 THEN 1 ELSE 0 END) AS kefu_users,
    SUM(CASE WHEN is_kefu = 0 OR is_kefu IS NULL THEN 1 ELSE 0 END) AS normal_users
FROM eb_chat_user;

-- ============================================
-- 步骤2：为现有客服创建 chat_user 记录
-- ============================================

-- 方式1：为没有 user_id 的客服创建新的 chat_user 记录
INSERT INTO eb_chat_user (
    nickname, 
    avatar, 
    phone, 
    appid, 
    is_kefu, 
    is_tourist, 
    online, 
    type, 
    is_delete,
    group_id,
    create_time, 
    update_time
)
SELECT 
    s.nickname,
    s.avatar,
    s.phone,
    s.appid,
    1 AS is_kefu,                    -- ⭐ 标记为客服
    0 AS is_tourist,
    s.online,
    0 AS type,                        -- PC端
    0 AS is_delete,
    0 AS group_id,
    NOW() AS create_time,
    NOW() AS update_time
FROM eb_chat_service s
WHERE s.user_id IS NULL               -- 只处理没有关联 user_id 的客服
  AND s.phone IS NOT NULL             -- 必须有手机号（用于唯一标识）
  AND s.phone != '';

-- ============================================
-- 步骤3：更新客服表的 user_id 字段
-- ============================================

-- 通过 phone + appid 关联更新
UPDATE eb_chat_service s
INNER JOIN eb_chat_user u 
    ON s.phone = u.phone 
    AND s.appid = u.appid 
    AND u.is_kefu = 1
SET s.user_id = u.id,
    s.update_time = UNIX_TIMESTAMP()
WHERE s.user_id IS NULL
  AND s.phone IS NOT NULL
  AND s.phone != '';

-- ============================================
-- 步骤4：处理已有 user_id 但 chat_user 记录未标记为客服的情况
-- ============================================

-- 更新已关联但未标记为客服的 chat_user 记录
UPDATE eb_chat_user u
INNER JOIN eb_chat_service s ON u.id = s.user_id
SET u.is_kefu = 1,
    u.update_time = NOW()
WHERE u.is_kefu != 1 OR u.is_kefu IS NULL;

-- ============================================
-- 步骤5：验证迁移结果
-- ============================================

-- 检查是否还有客服没有关联 user_id
SELECT 
    s.id,
    s.nickname,
    s.phone,
    s.appid,
    s.user_id
FROM eb_chat_service s
WHERE s.user_id IS NULL
ORDER BY s.id;

-- 检查客服关联的 chat_user 记录是否正确标记
SELECT 
    s.id AS service_id,
    s.nickname AS service_nickname,
    s.user_id,
    u.id AS user_id,
    u.nickname AS user_nickname,
    u.is_kefu,
    u.is_tourist
FROM eb_chat_service s
LEFT JOIN eb_chat_user u ON s.user_id = u.id
WHERE s.user_id IS NOT NULL
ORDER BY s.id;

-- 统计迁移结果
SELECT 
    '客服总数' AS metric,
    COUNT(*) AS count
FROM eb_chat_service
UNION ALL
SELECT 
    '已关联 user_id 的客服',
    COUNT(*)
FROM eb_chat_service
WHERE user_id IS NOT NULL
UNION ALL
SELECT 
    '未关联 user_id 的客服',
    COUNT(*)
FROM eb_chat_service
WHERE user_id IS NULL
UNION ALL
SELECT 
    '标记为客服的 chat_user',
    COUNT(*)
FROM eb_chat_user
WHERE is_kefu = 1;

-- ============================================
-- 步骤6：处理特殊情况（可选）
-- ============================================

-- 如果有客服没有手机号，需要手动处理
-- 可以使用 account 字段或其他唯一标识

-- 查看没有手机号的客服
SELECT 
    id,
    nickname,
    account,
    phone,
    appid
FROM eb_chat_service
WHERE (phone IS NULL OR phone = '')
  AND user_id IS NULL;

-- 为没有手机号的客服创建 chat_user（使用 account 作为唯一标识）
-- 注意：这需要根据实际情况调整
/*
INSERT INTO eb_chat_user (
    nickname, 
    avatar, 
    appid, 
    is_kefu, 
    is_tourist, 
    online, 
    type, 
    is_delete,
    group_id,
    create_time, 
    update_time
)
SELECT 
    s.nickname,
    s.avatar,
    s.appid,
    1 AS is_kefu,
    0 AS is_tourist,
    s.online,
    0 AS type,
    0 AS is_delete,
    0 AS group_id,
    NOW() AS create_time,
    NOW() AS update_time
FROM eb_chat_service s
WHERE (s.phone IS NULL OR s.phone = '')
  AND s.user_id IS NULL;

-- 然后手动更新 user_id
-- UPDATE eb_chat_service SET user_id = ? WHERE id = ?;
*/

-- ============================================
-- 步骤7：清理和优化（可选）
-- ============================================

-- 删除重复的 chat_user 记录（如果有）
-- 注意：谨慎执行，建议先备份
/*
DELETE u1 FROM eb_chat_user u1
INNER JOIN eb_chat_user u2 
WHERE u1.id > u2.id 
  AND u1.phone = u2.phone 
  AND u1.appid = u2.appid
  AND u1.is_kefu = 1
  AND u2.is_kefu = 1;
*/

-- ============================================
-- 回滚脚本（如果需要）
-- ============================================

-- 如果迁移出现问题，可以使用以下脚本回滚
-- 注意：这会删除所有标记为客服的 chat_user 记录

/*
-- 备份当前状态
CREATE TABLE eb_chat_user_backup AS SELECT * FROM eb_chat_user WHERE is_kefu = 1;
CREATE TABLE eb_chat_service_backup AS SELECT * FROM eb_chat_service;

-- 清除客服的 user_id 关联
UPDATE eb_chat_service SET user_id = NULL WHERE user_id IN (
    SELECT id FROM eb_chat_user WHERE is_kefu = 1
);

-- 删除标记为客服的 chat_user 记录
DELETE FROM eb_chat_user WHERE is_kefu = 1;

-- 恢复备份（如果需要）
-- INSERT INTO eb_chat_user SELECT * FROM eb_chat_user_backup;
-- UPDATE eb_chat_service s 
-- INNER JOIN eb_chat_service_backup b ON s.id = b.id
-- SET s.user_id = b.user_id;
*/

-- ============================================
-- 使用说明
-- ============================================

/*
1. 执行前备份数据库：
   mysqldump -u root -p crmchat > crmchat_backup_$(date +%Y%m%d_%H%M%S).sql

2. 按顺序执行步骤1-5的SQL语句

3. 检查步骤5的验证结果，确保迁移成功

4. 如果有特殊情况（如没有手机号的客服），执行步骤6

5. 测试客服互聊功能：
   - 客服A登录，查看会话列表
   - 客服A向客服B发送消息
   - 客服B登录，查看是否收到消息
   - 客服B回复消息
   - 客服A查看是否收到回复

6. 如果出现问题，使用回滚脚本恢复

预计影响：
- 新增 chat_user 记录数 = 没有 user_id 的客服数
- 更新 chat_service 记录数 = 所有客服数
- 更新 chat_user 记录数 = 已有 user_id 但未标记为客服的记录数

注意事项：
- 确保在测试环境先执行
- 执行前备份数据库
- 检查 phone 字段的唯一性
- 如果有重复的 phone，需要先处理
*/

