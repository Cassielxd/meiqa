-- ============================================
-- IP地理位置功能诊断脚本
-- ============================================

-- 1. 检查表结构是否包含地理位置字段
SELECT '=== 1. 检查表结构 ===' as step;

SELECT 
    COLUMN_NAME as '字段名',
    COLUMN_TYPE as '字段类型',
    IS_NULLABLE as '允许NULL',
    COLUMN_DEFAULT as '默认值',
    COLUMN_COMMENT as '注释'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'eb_chat_user'
  AND COLUMN_NAME IN ('country', 'region', 'city', 'isp', 'geo_info', 'geo_updated_time', 'last_ip')
ORDER BY ORDINAL_POSITION;

-- 如果上面的查询返回0行，说明字段不存在，需要执行：
-- source backend/security-enterprise-tenant/crmchat/db/add_geo_fields.sql

-- ============================================

-- 2. 检查游客用户数量
SELECT '=== 2. 检查游客用户 ===' as step;

SELECT 
    COUNT(*) as '游客总数',
    SUM(CASE WHEN last_ip IS NOT NULL AND last_ip != '' THEN 1 ELSE 0 END) as '有IP的游客',
    SUM(CASE WHEN country IS NOT NULL AND country != '' THEN 1 ELSE 0 END) as '有地理位置的游客'
FROM eb_chat_user
WHERE is_tourist = 1;

-- ============================================

-- 3. 查看最近的游客记录
SELECT '=== 3. 最近的游客记录 ===' as step;

SELECT 
    id as 'ID',
    nickname as '昵称',
    last_ip as 'IP地址',
    country as '国家',
    region as '省份',
    city as '城市',
    isp as '运营商',
    FROM_UNIXTIME(geo_updated_time) as '地理位置更新时间',
    create_time as '创建时间'
FROM eb_chat_user
WHERE is_tourist = 1
ORDER BY id DESC
LIMIT 10;

-- ============================================

-- 4. 查看有地理位置信息的游客
SELECT '=== 4. 有地理位置信息的游客 ===' as step;

SELECT 
    id as 'ID',
    nickname as '昵称',
    last_ip as 'IP地址',
    CONCAT(country, '-', region, '-', city) as '地理位置',
    isp as '运营商',
    FROM_UNIXTIME(geo_updated_time) as '更新时间'
FROM eb_chat_user
WHERE is_tourist = 1
  AND country IS NOT NULL 
  AND country != ''
ORDER BY geo_updated_time DESC
LIMIT 10;

-- ============================================

-- 5. 统计各城市的游客分布
SELECT '=== 5. 游客城市分布 ===' as step;

SELECT 
    country as '国家',
    region as '省份',
    city as '城市',
    COUNT(*) as '游客数量'
FROM eb_chat_user
WHERE is_tourist = 1
  AND country IS NOT NULL 
  AND country != ''
GROUP BY country, region, city
ORDER BY COUNT(*) DESC
LIMIT 10;

-- ============================================

-- 6. 查找没有地理位置的游客（可能需要触发更新）
SELECT '=== 6. 没有地理位置的游客 ===' as step;

SELECT 
    id as 'ID',
    nickname as '昵称',
    last_ip as 'IP地址',
    create_time as '创建时间'
FROM eb_chat_user
WHERE is_tourist = 1
  AND (country IS NULL OR country = '')
ORDER BY id DESC
LIMIT 10;

-- ============================================

-- 7. 手动清空某个游客的IP（用于测试）
-- 取消注释下面的SQL来清空指定游客的IP，强制触发地理位置更新

/*
SELECT '=== 7. 清空测试游客的IP（强制触发更新） ===' as step;

UPDATE eb_chat_user 
SET 
    last_ip = '', 
    country = '', 
    region = '', 
    city = '', 
    isp = '', 
    geo_info = NULL, 
    geo_updated_time = 0
WHERE id = 1  -- 替换为实际的游客ID
  AND is_tourist = 1;

SELECT '已清空游客ID=1的地理位置信息，下次发送消息时会重新获取' as result;
*/

-- ============================================

-- 8. 查看最近发送消息的游客
SELECT '=== 8. 最近发送消息的游客 ===' as step;

SELECT DISTINCT
    u.id as '游客ID',
    u.nickname as '昵称',
    u.last_ip as 'IP地址',
    u.country as '国家',
    u.city as '城市',
    MAX(r.add_time) as '最后消息时间'
FROM eb_chat_user u
INNER JOIN eb_chat_service_record r ON u.id = r.user_id
WHERE u.is_tourist = 1
GROUP BY u.id, u.nickname, u.last_ip, u.country, u.city
ORDER BY MAX(r.add_time) DESC
LIMIT 10;

-- ============================================
-- 诊断结果说明
-- ============================================

/*
✅ 正常情况：
1. 步骤1应该返回7个字段（country, region, city, isp, geo_info, geo_updated_time, last_ip）
2. 步骤2显示有游客用户，且部分游客有IP和地理位置
3. 步骤4显示有地理位置信息的游客列表
4. 步骤5显示游客的城市分布统计

❌ 异常情况：
1. 步骤1返回0行 → 需要执行 add_geo_fields.sql 添加字段
2. 步骤2显示游客总数为0 → 没有游客用户，需要先有游客发送消息
3. 步骤4返回0行 → 地理位置功能未生效，需要检查：
   - 后端日志是否有错误
   - 配置文件 geo.mock-local-ip 是否正确
   - 游客是否发送过消息
   - IP地址是否变化

🔧 修复步骤：
1. 如果字段不存在：执行 add_geo_fields.sql
2. 如果有游客但没有地理位置：
   - 检查后端日志
   - 使用步骤7清空某个游客的IP
   - 让该游客重新发送消息
   - 观察后端日志和数据库变化
*/

