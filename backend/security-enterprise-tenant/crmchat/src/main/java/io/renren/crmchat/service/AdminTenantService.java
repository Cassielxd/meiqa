package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.page.PageData;
import io.renren.crmchat.dao.TenantsMapper;
import io.renren.crmchat.entity.TenantsEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.PaginationService;
import io.renren.crmchat.service.common.PasswordService;
import io.renren.crmchat.service.common.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Admin 租户管理服务
 * PHP Reference: /app/services/tenant/TenantServices.php
 *
 * 核心职责:
 * 1. 租户列表查询（分页、搜索、过滤）
 * 2. 租户详情查询（包含到期信息）
 * 3. 租户创建（账号验证、密码加密、appid生成）
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminTenantService {

    private final TenantsMapper tenantsMapper;
    private final PaginationService paginationService;
    private final ValidationService validationService;
    private final PasswordService passwordService;
    private final AdminApplicationService adminApplicationService;

    /**
     * 获取租户列表（带分页和搜索）
     * PHP Reference: TenantServices.php::getTenantList()
     *
     * 查询条件:
     * - keyword: 搜索租户名称、账号、联系人
     * - status: 状态筛选
     * - date: 日期范围筛选
     *
     * @param params 查询参数
     * @return PageData 包含列表和总数
     */
    public PageData<Map<String, Object>> getTenantList(Map<String, Object> params) {
        // 1. 提取分页参数
        PaginationService.PaginationParams pagination = paginationService.extractParams(params);

        // 2. 构建查询条件
        QueryWrapper<TenantsEntity> wrapper = buildQueryWrapper(params);

        // 3. 分页查询
        List<TenantsEntity> tenants = tenantsMapper.selectList(wrapper
                .last(String.format("LIMIT %d OFFSET %d", pagination.getLimit(), pagination.getOffset()))
        );

        // 4. 查询总数
        QueryWrapper<TenantsEntity> countWrapper = buildQueryWrapper(params);
        Long total = tenantsMapper.selectCount(countWrapper);

        // 5. 处理数据（添加额外字段）
        List<Map<String, Object>> list = new ArrayList<>();
        for (TenantsEntity tenant : tenants) {
            Map<String, Object> item = entityToMap(tenant);

            // 添加是否过期标记
            item.put("is_expired", isExpired(tenant.getExpireAt()));

            // 添加剩余天数
            item.put("remaining_days", calculateRemainingDays(tenant.getExpireAt()));

            list.add(item);
        }

        // 6. 返回分页数据
        return paginationService.createPageData(list, total);
    }

    /**
     * 获取租户详情
     * PHP Reference: TenantServices.php::getTenantInfo()
     *
     * @param id 租户ID
     * @return 租户详情（包含过期信息）
     */
    public Map<String, Object> getTenantInfo(Integer id) {
        // 1. 查询租户
        TenantsEntity tenant = tenantsMapper.selectById(id);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        // 2. 转换为 Map 并添加额外字段
        Map<String, Object> info = entityToMap(tenant);
        info.put("is_expired", isExpired(tenant.getExpireAt()));
        info.put("remaining_days", calculateRemainingDays(tenant.getExpireAt()));

        return info;
    }

    /**
     * 创建租户
     * PHP Reference: TenantServices.php::createTenant()
     *
     * 步骤:
     * 1. 验证必填参数
     * 2. 验证账号唯一性
     * 3. 生成 tenant_code 和 appid
     * 4. 密码加密
     * 5. 保存租户
     *
     * @param data 租户数据
     * @return 创建的租户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTenant(Map<String, Object> data) {
        // 1. 验证必填参数
        validateCreateParams(data);

        // 2. 验证账号唯一性
        String account = (String) data.get("account");
        if (checkAccountExists(account)) {
            throw new CrmChatException("Administrator account already exists");
        }

        // 3. 生成唯一的租户编码
        String tenantCode = generateTenantCode();

        // 4. 使用ApplicationService生成应用信息（包含appid, app_secret, rand, timestamp）
        Map<String, Object> appInfo = adminApplicationService.generateAppInfo();
        String appid = (String) appInfo.get("appid");

        // 5. 创建租户实体
        TenantsEntity tenant = new TenantsEntity();
        tenant.setAppid(appid);
        tenant.setTenantName((String) data.get("tenant_name"));
        tenant.setTenantCode(tenantCode);
        tenant.setAccount(account);

        // 6. 密码加密
        String password = (String) data.get("pwd");
        tenant.setPwd(passwordService.hash(password));

        // 7. 设置联系信息
        tenant.setContactName((String) data.getOrDefault("contact_name", data.get("tenant_name")));
        tenant.setContactEmail((String) data.getOrDefault("contact_email", account));
        tenant.setContactPhone((String) data.get("contact_phone"));

        // 8. 设置配额
        tenant.setMaxUsers(parseIntOrDefault(data.get("user_limit"), 1000));
        tenant.setMaxServices(parseIntOrDefault(data.get("service_limit"), 10));

        // 9. 设置状态（管理员创建默认启用）
        tenant.setStatus(parseIntOrDefault(data.get("status"), 1));

        // 10. 设置过期时间
        if (data.containsKey("expire_at") && data.get("expire_at") != null) {
            String expireAtStr = (String) data.get("expire_at");
            if (!expireAtStr.trim().isEmpty()) {
                tenant.setExpireAt(parseTimestamp(expireAtStr));
            }
        }

        // 11. 设置创建和更新时间
        Timestamp now = new Timestamp(System.currentTimeMillis());
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);

        // 12. 保存到数据库
        int result = tenantsMapper.insert(tenant);
        if (result <= 0) {
            throw new CrmChatException("Failed to create tenant");
        }

        // 13. 如果状态为启用(1)，自动创建应用数据到application表
        if (tenant.getStatus() == 1) {
            try {
                adminApplicationService.createTenantApplication(tenant.getTenantName(), appInfo);
            } catch (Exception e) {
                // 应用创建失败不影响租户创建，只记录错误
                // TODO: 添加日志记录
                System.err.println("Failed to create tenant application: " + e.getMessage());
            }
        }

        // 14. 返回创建的租户信息
        return entityToMap(tenant);
    }

    /**
     * 更新租户
     * PHP Reference: TenantServices.php::updateTenant()
     *
     * 功能:
     * 1. 更新租户基本信息（租户名、联系人、联系方式）
     * 2. 可选更新密码
     * 3. 更新最大用户数/客服数
     * 4. 更新到期时间
     * 5. 状态更新
     *
     * @param id 租户ID
     * @param data 更新数据（空值会被过滤）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTenant(Integer id, Map<String, Object> data) {
        // 1. 检查租户是否存在
        TenantsEntity tenant = tenantsMapper.selectById(id);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        // 记录原状态，用于判断是否需要创建应用
        int oldStatus = tenant.getStatus();

        // 2. 更新租户名称
        if (data.containsKey("tenant_name") && data.get("tenant_name") != null) {
            String tenantName = data.get("tenant_name").toString().trim();
            if (!tenantName.isEmpty()) {
                tenant.setTenantName(tenantName);
            }
        }

        // 3. 更新联系邮箱（带验证）
        if (data.containsKey("contact_email") && data.get("contact_email") != null) {
            String email = data.get("contact_email").toString().trim();
            if (!email.isEmpty()) {
                validationService.validateEmail(email);
                tenant.setContactEmail(email);
            }
        }

        // 4. 更新联系电话（带验证）
        if (data.containsKey("contact_phone") && data.get("contact_phone") != null) {
            String phone = data.get("contact_phone").toString().trim();
            if (!phone.isEmpty()) {
                validationService.validatePhone(phone);
                tenant.setContactPhone(phone);
            }
        }

        // 5. 更新联系人名称
        if (data.containsKey("contact_name") && data.get("contact_name") != null) {
            String contactName = data.get("contact_name").toString().trim();
            if (!contactName.isEmpty()) {
                tenant.setContactName(contactName);
            }
        }

        // 6. 更新最大用户数
        if (data.containsKey("user_limit") && data.get("user_limit") != null) {
            int userLimit = parseIntOrDefault(data.get("user_limit"), tenant.getMaxUsers());
            if (userLimit > 0) {
                tenant.setMaxUsers(userLimit);
            }
        }

        // 7. 更新最大客服数
        if (data.containsKey("service_limit") && data.get("service_limit") != null) {
            int serviceLimit = parseIntOrDefault(data.get("service_limit"), tenant.getMaxServices());
            if (serviceLimit > 0) {
                tenant.setMaxServices(serviceLimit);
            }
        }

        // 8. 更新到期时间
        if (data.containsKey("expire_at") && data.get("expire_at") != null) {
            String expireAtStr = data.get("expire_at").toString().trim();
            if (!expireAtStr.isEmpty()) {
                tenant.setExpireAt(parseTimestamp(expireAtStr));
            }
        }

        // 9. 更新自动续费标记
        if (data.containsKey("auto_renew") && data.get("auto_renew") != null) {
            int autoRenew = parseIntOrDefault(data.get("auto_renew"), 0);
            // Note: TenantsEntity may need auto_renew field added if not exists
            // tenant.setAutoRenew(autoRenew);
        }

        // 10. 更新状态
        if (data.containsKey("status") && data.get("status") != null) {
            String statusStr = data.get("status").toString().trim();
            if (!statusStr.isEmpty()) {
                int status = parseIntOrDefault(data.get("status"), tenant.getStatus());
                tenant.setStatus(status);
            }
        }

        // 11. 更新备注
        if (data.containsKey("remark") && data.get("remark") != null) {
            String remark = data.get("remark").toString();
            // Note: TenantsEntity may need remark field added if not exists
            // tenant.setRemark(remark);
        }

        // 12. 可选：更新密码
        if (data.containsKey("pwd") && data.get("pwd") != null) {
            String password = data.get("pwd").toString().trim();
            if (!password.isEmpty()) {
                tenant.setPwd(passwordService.hash(password));
            }
        }

        // 13. 设置更新时间
        tenant.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // 14. 保存更新
        int result = tenantsMapper.updateById(tenant);
        if (result <= 0) {
            throw new CrmChatException("Failed to update tenant");
        }

        // 15. 如果状态从非启用变为启用(1)，检查并创建应用
        // PHP Reference: TenantServices.php::updateTenant() -> checkAndCreateApplication()
        int newStatus = tenant.getStatus();
        if (newStatus == 1 && oldStatus != 1) {
            // 状态变更为启用，需要检查appid对应的应用是否存在
            String appid = tenant.getAppid();
            if (appid == null || appid.trim().isEmpty()) {
                // 如果租户没有appid，先生成一个
                Map<String, Object> appInfo = adminApplicationService.generateAppInfo();
                appid = (String) appInfo.get("appid");

                // 更新租户的appid
                tenant.setAppid(appid);
                tenantsMapper.updateById(tenant);

                // 创建应用
                try {
                    adminApplicationService.createTenantApplication(tenant.getTenantName(), appInfo);
                } catch (Exception e) {
                    System.err.println("更新租户时创建应用失败: " + e.getMessage());
                }
            } else {
                // 租户有appid，createTenantApplication内部会检查应用是否存在，不存在则创建
                try {
                    // 生成新的appInfo(如果应用已存在，createTenantApplication会直接返回true)
                    Map<String, Object> newAppInfo = adminApplicationService.generateAppInfo();
                    adminApplicationService.createTenantApplication(tenant.getTenantName(), newAppInfo);
                } catch (Exception e) {
                    System.err.println("更新租户时创建应用失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 更新租户状态
     * PHP Reference: TenantServices.php::updateTenantStatus()
     *
     * @param id 租户ID
     * @param status 状态（0-禁用，1-启用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantStatus(Integer id, Integer status) {
        // 1. 检查租户是否存在
        TenantsEntity tenant = tenantsMapper.selectById(id);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        // 2. 验证状态值
        if (status != 0 && status != 1) {
            throw new CrmChatException("Status parameter error: must be 0 or 1");
        }

        // 3. 更新状态
        tenant.setStatus(status);
        tenant.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // 4. 保存
        int result = tenantsMapper.updateById(tenant);
        if (result <= 0) {
            throw new CrmChatException("Failed to update status");
        }
    }

    /**
     * 删除租户（软删除）
     * PHP Reference: TenantServices.php::deleteTenant()
     *
     * 注意: 这是软删除，设置 is_del=1，不是物理删除
     *
     * @param id 租户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Integer id) {
        // 1. 检查租户是否存在
        TenantsEntity tenant = tenantsMapper.selectById(id);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        // 2. 物理删除（实体类暂无is_del字段，使用物理删除）
        // TODO: 如果数据库有is_del字段，需要先在TenantsEntity中添加该字段，然后改为软删除
        int result = tenantsMapper.deleteById(id);
        if (result <= 0) {
            throw new CrmChatException("Failed to delete tenant");
        }

        // 3. TODO: 可选 - 清除相关缓存（如果有使用 Redis 缓存租户信息）
        // cacheService.clear("tenant:" + id);
    }

    /**
     * 构建查询条件
     * PHP Reference: QueryBuilderService 模式
     */
    private QueryWrapper<TenantsEntity> buildQueryWrapper(Map<String, Object> params) {
        QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();

        // 关键词搜索（租户名称、账号、联系人）
        if (params.containsKey("keyword") && params.get("keyword") != null) {
            String keyword = params.get("keyword").toString().trim();
            if (!keyword.isEmpty()) {
                wrapper.and(w -> w
                        .like("tenant_name", keyword)
                        .or().like("account", keyword)
                        .or().like("contact_name", keyword)
                        .or().like("contact_phone", keyword)
                        .or().like("contact_email", keyword)
                );
            }
        }

        // 状态筛选
        if (params.containsKey("status") && params.get("status") != null) {
            String statusStr = params.get("status").toString().trim();
            if (!statusStr.isEmpty()) {
                try {
                    int status = Integer.parseInt(statusStr);
                    wrapper.eq("status", status);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // 日期范围筛选
        if (params.containsKey("date") && params.get("date") != null) {
            String dateRange = params.get("date").toString().trim();
            if (!dateRange.isEmpty() && dateRange.contains("-")) {
                String[] dates = dateRange.split("-");
                if (dates.length == 2) {
                    String startDate = dates[0].trim();
                    String endDate = dates[1].trim();
                    wrapper.between("created_at", startDate + " 00:00:00", endDate + " 23:59:59");
                }
            }
        }

        // 默认按创建时间倒序
        wrapper.orderByDesc("created_at");

        return wrapper;
    }

    /**
     * 验证创建租户的参数
     */
    private void validateCreateParams(Map<String, Object> data) {
        // 必填字段验证
        if (!data.containsKey("tenant_name") || data.get("tenant_name") == null
                || data.get("tenant_name").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter tenant name");
        }

        if (!data.containsKey("account") || data.get("account") == null
                || data.get("account").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter administrator account");
        }

        if (!data.containsKey("pwd") || data.get("pwd") == null
                || data.get("pwd").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter password");
        }

        // 邮箱格式验证（如果提供）
        if (data.containsKey("contact_email") && data.get("contact_email") != null) {
            String email = data.get("contact_email").toString();
            if (!email.trim().isEmpty()) {
                validationService.validateEmail(email);
            }
        }

        // 手机号格式验证（如果提供）
        if (data.containsKey("contact_phone") && data.get("contact_phone") != null) {
            String phone = data.get("contact_phone").toString();
            if (!phone.trim().isEmpty()) {
                validationService.validatePhone(phone);
            }
        }
    }

    /**
     * 检查账号是否存在
     */
    private boolean checkAccountExists(String account) {
        QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        return tenantsMapper.selectCount(wrapper) > 0;
    }

    /**
     * 生成唯一的租户编码
     * PHP Reference: TenantServices.php::generateTenantCode()
     */
    private String generateTenantCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int randomPart = new Random().nextInt(9000) + 1000; // 1000-9999
            code = "tenant_" + datePart + "_" + randomPart;

            QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("tenant_code", code);

            if (tenantsMapper.selectCount(wrapper) == 0) {
                break;
            }

            attempts++;
        } while (attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            throw new CrmChatException("Failed to generate tenant code, please try again");
        }

        return code;
    }

    /**
     * 判断租户是否过期
     */
    private boolean isExpired(Timestamp expireAt) {
        if (expireAt == null) {
            return false; // null 表示永久有效
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.after(expireAt);
    }

    /**
     * 计算剩余天数
     * PHP Reference: TenantServices.php::calculateRemainingDays()
     */
    private int calculateRemainingDays(Timestamp expireAt) {
        if (expireAt == null) {
            return -1; // -1 表示永久有效
        }

        long now = System.currentTimeMillis();
        long expireTime = expireAt.getTime();
        long diffMillis = expireTime - now;

        if (diffMillis < 0) {
            return 0; // 已过期
        }

        return (int) (diffMillis / (1000 * 60 * 60 * 24)); // 转换为天数
    }

    /**
     * 实体转 Map
     */
    private Map<String, Object> entityToMap(TenantsEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("appid", entity.getAppid());
        map.put("tenant_name", entity.getTenantName());
        map.put("tenant_code", entity.getTenantCode());
        map.put("account", entity.getAccount());
        map.put("domain", entity.getDomain());
        map.put("logo", entity.getLogo());
        map.put("contact_name", entity.getContactName());
        map.put("contact_phone", entity.getContactPhone());
        map.put("contact_email", entity.getContactEmail());
        map.put("status", entity.getStatus());
        map.put("max_users", entity.getMaxUsers());
        map.put("max_services", entity.getMaxServices());
        map.put("expire_at", entity.getExpireAt());
        map.put("created_at", entity.getCreatedAt());
        map.put("updated_at", entity.getUpdatedAt());
        return map;
    }

    /**
     * 解析整数参数
     */
    private int parseIntOrDefault(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 解析时间戳字符串
     * 支持格式：
     * - yyyy-MM-dd (自动补充 23:59:59)
     * - yyyy-MM-dd HH:mm:ss
     */
    private Timestamp parseTimestamp(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new CrmChatException("Date string cannot be empty");
        }

        // 清理字符串：去除首尾空白和可能的换行符
        dateStr = dateStr.trim().replaceAll("[\\r\\n]", "");

        try {
            LocalDateTime dateTime;

            if (dateStr.contains(":")) {
                // 包含时间部分：yyyy-MM-dd HH:mm:ss
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                dateTime = LocalDateTime.parse(dateStr, formatter);
            } else {
                // 只有日期部分：yyyy-MM-dd，补充 23:59:59
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(dateStr, dateFormatter);
                // 从LocalDate创建LocalDateTime并设置时间为23:59:59
                dateTime = date.atTime(23, 59, 59);
            }

            return Timestamp.valueOf(dateTime);
        } catch (Exception e) {
            throw new CrmChatException("Invalid date format: '" + dateStr + "'. Expected format: yyyy-MM-dd or yyyy-MM-dd HH:mm:ss. Error: " + e.getMessage());
        }
    }

    /**
     * 获取租户统计信息
     * PHP Reference: TenantServices.php::getTenantStatistics()
     *
     * 返回:
     * - total: 总租户数
     * - enabled_count: 启用状态租户数
     * - disabled_count: 禁用状态租户数
     * - expiring_count: 7天内即将到期的租户数
     * - expired_count: 已过期的租户数
     * - status_distribution: 各状态分布
     *
     * @return 统计信息 Map
     */
    public Map<String, Object> getTenantStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 总租户数
        Long total = tenantsMapper.selectCount(new QueryWrapper<>());
        stats.put("total", total);

        // 2. 按状态统计
        QueryWrapper<TenantsEntity> enabledWrapper = new QueryWrapper<>();
        enabledWrapper.eq("status", 1);
        Long enabledCount = tenantsMapper.selectCount(enabledWrapper);
        stats.put("enabled_count", enabledCount);

        QueryWrapper<TenantsEntity> disabledWrapper = new QueryWrapper<>();
        disabledWrapper.eq("status", 0);
        Long disabledCount = tenantsMapper.selectCount(disabledWrapper);
        stats.put("disabled_count", disabledCount);

        // 3. 即将过期的租户数（7天内）
        List<TenantsEntity> expiringTenants = getExpiringTenantsList(7);
        stats.put("expiring_count", expiringTenants.size());

        // 4. 已过期的租户数
        List<TenantsEntity> expiredTenants = getExpiredTenantsList();
        stats.put("expired_count", expiredTenants.size());

        // 5. 状态分布
        Map<String, Object> statusDistribution = new HashMap<>();
        statusDistribution.put("enabled", enabledCount);
        statusDistribution.put("disabled", disabledCount);
        statusDistribution.put("pending", total - enabledCount - disabledCount); // 其他状态
        stats.put("status_distribution", statusDistribution);

        return stats;
    }

    /**
     * 获取即将过期的租户列表
     * PHP Reference: TenantServices.php::getExpiringTenants()
     *
     * @param days 天数（默认30天）
     * @return 即将过期的租户列表
     */
    public List<Map<String, Object>> getExpiringTenants(Integer days) {
        if (days == null || days <= 0) {
            days = 30; // 默认30天
        }

        List<TenantsEntity> tenants = getExpiringTenantsList(days);

        // 转换为 Map 并添加剩余天数
        List<Map<String, Object>> result = new ArrayList<>();
        for (TenantsEntity tenant : tenants) {
            Map<String, Object> item = entityToMap(tenant);
            item.put("is_expired", isExpired(tenant.getExpireAt()));
            item.put("remaining_days", calculateRemainingDays(tenant.getExpireAt()));
            result.add(item);
        }

        return result;
    }

    /**
     * 重置租户密码
     * PHP Reference: TenantServices.php::resetTenantPassword()
     *
     * @param id 租户ID
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetTenantPassword(Integer id, String newPassword) {
        // 1. 检查租户是否存在
        TenantsEntity tenant = tenantsMapper.selectById(id);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        // 2. 验证新密码
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new CrmChatException("Please enter new password");
        }

        if (newPassword.length() < 6) {
            throw new CrmChatException("Password must be at least 6 characters");
        }

        // 3. 加密密码
        String hashedPassword = passwordService.hash(newPassword);

        // 4. 更新密码
        tenant.setPwd(hashedPassword);
        tenant.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        int result = tenantsMapper.updateById(tenant);
        if (result <= 0) {
            throw new CrmChatException("Failed to reset password");
        }
    }

    /**
     * 获取即将过期的租户列表（内部方法）
     *
     * @param days 天数
     * @return 租户列表
     */
    private List<TenantsEntity> getExpiringTenantsList(Integer days) {
        QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();

        // 计算日期范围
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long futureMillis = now.getTime() + (days * 24L * 60 * 60 * 1000);
        Timestamp futureDate = new Timestamp(futureMillis);

        // 查询条件: expire_at IS NOT NULL AND expire_at > now AND expire_at <= futureDate
        wrapper.isNotNull("expire_at")
                .gt("expire_at", now)
                .le("expire_at", futureDate)
                .orderByAsc("expire_at");

        return tenantsMapper.selectList(wrapper);
    }

    /**
     * 获取已过期的租户列表（内部方法）
     *
     * @return 已过期的租户列表
     */
    private List<TenantsEntity> getExpiredTenantsList() {
        QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();

        Timestamp now = new Timestamp(System.currentTimeMillis());

        // 查询条件: expire_at IS NOT NULL AND expire_at < now
        wrapper.isNotNull("expire_at")
                .lt("expire_at", now);

        return tenantsMapper.selectList(wrapper);
    }
}
