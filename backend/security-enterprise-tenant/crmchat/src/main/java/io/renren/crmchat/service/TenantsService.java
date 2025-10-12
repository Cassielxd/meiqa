package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.common.constant.TenantStatus;
import io.renren.crmchat.dao.TenantsMapper;
import io.renren.crmchat.dto.tenant.TenantRegisterDTO;
import io.renren.crmchat.entity.TenantsEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.CaptchaService;
import io.renren.crmchat.service.common.PasswordService;
import io.renren.crmchat.service.common.TokenService;
import io.renren.crmchat.service.common.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 租户服务
 * 对齐 PHP 端业务逻辑。
 */
@Service
@AllArgsConstructor
public class TenantsService {

    private final TenantsMapper tenantsMapper;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final ValidationService validationService;
    private final CaptchaService captchaService;

    /**
     * 租户登录
     */
    public Map<String, Object> login(String account, String password) {
        TenantsEntity tenantInfo = verifyLogin(account, password);

        String token = tokenService.generateTenantToken(
                Long.valueOf(tenantInfo.getId()),
                tenantInfo.getAccount(),
                tenantInfo.getAppid()
        );

        Long expiresTime = tokenService.getTokenExpireAt(token);

        TenantsEntity update = new TenantsEntity();
        update.setId(tenantInfo.getId());
        update.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        tenantsMapper.updateById(update);

        Map<String, Object> tenantInfoMap = buildTenantInfo(tenantInfo);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        if (expiresTime != null) {
            result.put("expires_time", expiresTime);
        }
        result.put("tenant_info", tenantInfoMap);
        result.put("user_info", tenantInfoMap);
        return result;
    }

    private TenantsEntity verifyLogin(String account, String password) {
        QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        TenantsEntity tenantInfo = tenantsMapper.selectOne(wrapper);

        if (tenantInfo == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        TenantStatus status = TenantStatus.fromCode(tenantInfo.getStatus());
        if (status == null) {
            throw new CrmChatException("Abnormal tenant status: unknown");
        }
        if (status == TenantStatus.DISABLED) {
            throw new CrmChatException("Tenant has been disabled");
        }
        if (status != TenantStatus.APPROVED) {
            throw new CrmChatException("Abnormal tenant status: " + status.getLabel());
        }

        if (isTenantExpired(tenantInfo.getExpireAt())) {
            throw new CrmChatException("Tenant has expired, please contact administrator");
        }

        passwordService.verifyOrThrow(
                password,
                tenantInfo.getPwd(),
                "Incorrect account or password, please try again"
        );

        return tenantInfo;
    }

    /**
     * 登录页信息（保持默认实现，后续可接入系统配置）。
     */
    public Map<String, Object> getLoginInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("logo_square", "/logo.png");
        info.put("logo_rectangle", "/logo_rectangle.png");
        info.put("login_logo", "/login_logo.png");
        info.put("slide", new String[]{});
        info.put("site_name", "CRMChat Tenant Management Platform");
        return info;
    }

    /**
     * 租户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(TenantRegisterDTO registerDTO) {
        validationService.validatePasswordMatch(registerDTO.getPwd(), registerDTO.getConfirmPwd());

        String email = registerDTO.getContactEmail();
        if (email == null || email.trim().isEmpty()) {
            email = registerDTO.getAccount();
        }
        if (email == null || email.trim().isEmpty()) {
            throw new CrmChatException("Please enter contact email");
        }
        validationService.validateEmail(email);

        if (!captchaService.verifyCaptcha(email, registerDTO.getCaptcha())) {
            throw new CrmChatException("Verification code is incorrect or expired, please get a new one");
        }

        QueryWrapper<TenantsEntity> accountWrapper = new QueryWrapper<>();
        accountWrapper.eq("account", registerDTO.getAccount());
        if (tenantsMapper.selectCount(accountWrapper) > 0) {
            throw new CrmChatException("Account already exists");
        }

        QueryWrapper<TenantsEntity> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("contact_email", email);
        if (tenantsMapper.selectCount(emailWrapper) > 0) {
            throw new CrmChatException("This email is already registered");
        }

        String tenantCode = generateTenantCode();
        String appid = generateAppId();

        String tenantName = registerDTO.getTenantName();
        if (tenantName == null || tenantName.trim().isEmpty()) {
            tenantName = email;
        }
        String contactName = registerDTO.getContactName();
        if (contactName == null || contactName.trim().isEmpty()) {
            contactName = email;
        }

        TenantsEntity tenant = new TenantsEntity();
        tenant.setTenantName(tenantName);
        tenant.setTenantCode(tenantCode);
        tenant.setAppid(appid);
        tenant.setAccount(registerDTO.getAccount());
        tenant.setPwd(passwordService.hash(registerDTO.getPwd()));
        tenant.setContactName(contactName);
        tenant.setContactPhone(registerDTO.getContactPhone());
        tenant.setContactEmail(email);
        tenant.setStatus(TenantStatus.PENDING.getCode());
        tenant.setMaxUsers(0);
        tenant.setMaxServices(0);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);

        if (tenantsMapper.insert(tenant) <= 0) {
            throw new CrmChatException("Registration failed, please try again");
        }

        captchaService.clearCaptcha(email);

        Map<String, Object> result = new HashMap<>();
        result.put("tenant_id", tenant.getId());
        result.put("tenant_code", tenantCode);
        result.put("status", TenantStatus.PENDING.getLabel());
        result.put("message", "Registered successfully, please wait for admin approval");
        return result;
    }

    private String generateTenantCode() {
        SecureRandom random = new SecureRandom();
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String code;
        do {
            int randomNum = random.nextInt(9000) + 1000;
            code = "tenant_" + dateStr + "_" + randomNum;

            QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("tenant_code", code);
            if (tenantsMapper.selectCount(wrapper) == 0) {
                break;
            }
        } while (true);

        return code;
    }

    private String generateAppId() {
        SecureRandom random = new SecureRandom();

        String appid;
        do {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            int randomNum = random.nextInt(9000) + 1000;
            appid = timestamp + randomNum;

            QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            if (tenantsMapper.selectCount(wrapper) == 0) {
                break;
            }
        } while (true);

        return appid;
    }

    public Map<String, Object> sendRegisterCaptcha(String email) {
        validationService.validateEmail(email);

        QueryWrapper<TenantsEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("contact_email", email);
        if (tenantsMapper.selectCount(wrapper) > 0) {
            throw new CrmChatException("This email is already registered");
        }

        String captcha = captchaService.sendCaptcha(email);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("captcha", captcha);
        result.put("message", "Verification code sent successfully, development environment code:" + captcha);
        return result;
    }

    public TenantsEntity getTenantInfo(Integer tenantId) {
        TenantsEntity tenant = tenantsMapper.selectById(tenantId);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }
        return tenant;
    }

    public Map<String, Object> getTenantStatus(Integer tenantId) {
        TenantsEntity tenant = tenantsMapper.selectById(tenantId);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", tenant.getStatus());
        TenantStatus status = TenantStatus.fromCode(tenant.getStatus());
        result.put("status_text", status != null ? status.getLabel() : "Unknown");
        boolean expired = isTenantExpired(tenant.getExpireAt());
        result.put("is_expired", expired);
        result.put("remaining_days", calculateRemainingDays(tenant.getExpireAt()));
        result.put("expire_at", tenant.getExpireAt() != null ? tenant.getExpireAt().toString() : null);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTenantProfile(Integer tenantId, Map<String, Object> data) {
        TenantsEntity tenant = tenantsMapper.selectById(tenantId);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        if (data.containsKey("tenant_name") && data.get("tenant_name") != null && !data.get("tenant_name").toString().trim().isEmpty()) {
            tenant.setTenantName(data.get("tenant_name").toString());
        }

        if (data.containsKey("contact_name") && data.get("contact_name") != null && !data.get("contact_name").toString().trim().isEmpty()) {
            tenant.setContactName(data.get("contact_name").toString());
        }

        if (data.containsKey("contact_phone") && data.get("contact_phone") != null && !data.get("contact_phone").toString().trim().isEmpty()) {
            tenant.setContactPhone(data.get("contact_phone").toString());
        }

        if (data.containsKey("domain") && data.get("domain") != null && !data.get("domain").toString().trim().isEmpty()) {
            tenant.setDomain(data.get("domain").toString());
        }

        if (data.containsKey("logo") && data.get("logo") != null && !data.get("logo").toString().trim().isEmpty()) {
            tenant.setLogo(data.get("logo").toString());
        }

        if (tenantsMapper.updateById(tenant) <= 0) {
            throw new CrmChatException("Failed to update");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Integer tenantId, String oldPassword, String newPassword, String confirmPassword) {
        TenantsEntity tenant = tenantsMapper.selectById(tenantId);
        if (tenant == null) {
            throw new CrmChatException("Tenant does not exist");
        }

        if (!passwordService.verify(oldPassword, tenant.getPwd())) {
            throw new CrmChatException("Old password is incorrect");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new CrmChatException("New passwords do not match");
        }

        if (newPassword.length() < 6) {
            throw new CrmChatException("New password must be at least 6 characters");
        }

        tenant.setPwd(passwordService.hash(newPassword));
        if (tenantsMapper.updateById(tenant) <= 0) {
            throw new CrmChatException("Failed to change password");
        }
    }

    private Map<String, Object> buildTenantInfo(TenantsEntity tenant) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", tenant.getId());
        info.put("appid", tenant.getAppid());
        info.put("tenant_name", tenant.getTenantName());
        info.put("tenant_code", tenant.getTenantCode());
        info.put("account", tenant.getAccount());
        info.put("domain", tenant.getDomain());
        info.put("logo", tenant.getLogo());
        info.put("contact_name", tenant.getContactName());
        info.put("contact_phone", tenant.getContactPhone());
        info.put("contact_email", tenant.getContactEmail());
        info.put("status", tenant.getStatus());
        info.put("max_users", tenant.getMaxUsers());
        info.put("max_services", tenant.getMaxServices());
        info.put("expire_at", tenant.getExpireAt() != null ? tenant.getExpireAt().toString() : null);
        boolean expired = isTenantExpired(tenant.getExpireAt());
        info.put("is_expired", expired);
        info.put("remaining_days", calculateRemainingDays(tenant.getExpireAt()));
        return info;
    }

    private boolean isTenantExpired(Timestamp expireAt) {
        if (expireAt == null) {
            return false;
        }
        return System.currentTimeMillis() >= expireAt.getTime();
    }

    private int calculateRemainingDays(Timestamp expireAt) {
        if (expireAt == null) {
            return -1;
        }
        long diff = expireAt.getTime() - System.currentTimeMillis();
        if (diff <= 0) {
            return 0;
        }
        return (int) (diff / (1000 * 60 * 60 * 24));
    }
}
