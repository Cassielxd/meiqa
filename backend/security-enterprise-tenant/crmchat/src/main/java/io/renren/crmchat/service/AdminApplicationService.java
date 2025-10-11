package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.utils.JsonUtils;
import io.renren.crmchat.CrmChatApplication;
import io.renren.crmchat.dao.ApplicationMapper;
import io.renren.crmchat.entity.ApplicationEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;

/**
 * Admin Application Service - 管理员应用管理
 * PHP Reference: Application.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminApplicationService {

    private final ApplicationMapper applicationMapper;

    /**
     * 获取应用列表
     * PHP Reference: Application.php::index()
     */
    public Map<String, Object> getList(Map<String, Object> where) {
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0);

        // name模糊查询
        String name = (String) where.get("name");
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name);
        }

        // 分页
        Integer page = (Integer) where.getOrDefault("page", 1);
        Integer limit = (Integer) where.getOrDefault("limit", 20);
        Page<ApplicationEntity> pageObj = new Page<>(page, limit);

        wrapper.orderByDesc("id");
        IPage<ApplicationEntity> result = applicationMapper.selectPage(pageObj, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("count", result.getTotal());
        return response;
    }

    /**
     * 获取创建表单
     * PHP Reference: Application.php::create()
     */
    public Map<String, Object> getCreateForm() {
        Map<String, Object> result = new HashMap<>();
        // TODO: FormBuilder pattern - 返回空form_rules
        result.put("form_rules", new Object[0]);
        return result;
    }

    /**
     * 保存应用
     * PHP Reference: Application.php::save()
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveApplication(Map<String, Object> data) {
        String icon = (String) data.get("icon");
        String name = (String) data.get("name");
        String introduce = (String) data.get("introduce");

        // 验证必填字段
        if (icon == null || icon.trim().isEmpty()) {
            throw new CrmChatException("Please select application icon");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Please enter application name");
        }

        // 检查名称是否已存在
        long count = applicationMapper.selectCount(
                new QueryWrapper<ApplicationEntity>()
                        .eq("name", name)
                        .eq("is_delete", 0)
        );
        if (count > 0) {
            throw new CrmChatException("Application name already exists");
        }

        // 生成应用凭证
        Random random = new Random();
        int rand = 1000 + random.nextInt(9000); // 1000-9999
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        String appid = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + timestamp + rand;
        String appSecret = md5(appid + timestamp + rand);

        // 生成token (简化版，实际可能需要加密)
        String tokenJson = String.format("{\"appid\":\"%s\",\"app_secret\":\"%s\",\"rand\":%d,\"timestamp\":%d}",
                appid, appSecret, rand, timestamp);
        String token = Base64.getEncoder().encodeToString(tokenJson.getBytes(StandardCharsets.UTF_8));
        String tokenMd5 = md5(token);

        // 创建应用
        ApplicationEntity app = new ApplicationEntity();
        app.setAppid(appid);
        app.setIcon(icon);
        app.setName(name);
        app.setIntroduce(introduce);
        app.setRand(rand);
        app.setTimestamp(timestamp);
        app.setAppSecret(appSecret);
        app.setToken(token);
        app.setTokenMd5(tokenMd5);
        app.setIsDelete(0);

        boolean success = applicationMapper.insert(app) > 0;
        if (!success) {
            throw new CrmChatException("Failed to save");
        }

        return "Saved successfully";
    }

    /**
     * 获取编辑表单
     * PHP Reference: Application.php::edit()
     */
    public Map<String, Object> getEditForm(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ApplicationEntity app = applicationMapper.selectById(id);
        if (app == null || app.getIsDelete() == 1) {
            throw new CrmChatException("Application does not exist");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("app", app);
        // TODO: FormBuilder pattern - 返回空form_rules
        result.put("form_rules", new Object[0]);
        return result;
    }

    /**
     * 更新应用
     * PHP Reference: Application.php::update()
     */
    @Transactional(rollbackFor = Exception.class)
    public String updateApplication(Integer id, Map<String, Object> data) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        String icon = (String) data.get("icon");
        String name = (String) data.get("name");
        String introduce = (String) data.get("introduce");

        // 验证必填字段
        if (icon == null || icon.trim().isEmpty()) {
            throw new CrmChatException("Please select application icon");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Please enter application name");
        }

        ApplicationEntity app = applicationMapper.selectById(id);
        if (app == null || app.getIsDelete() == 1) {
            throw new CrmChatException("Application does not exist");
        }

        // 更新字段
        app.setIcon(icon);
        app.setName(name);
        app.setIntroduce(introduce);

        boolean success = applicationMapper.updateById(app) > 0;
        if (!success) {
            throw new CrmChatException("Failed to save");
        }

        return "Saved successfully";
    }

    /**
     * 删除应用（软删除）
     * PHP Reference: Application.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public String deleteApplication(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ApplicationEntity app = applicationMapper.selectById(id);
        if (app == null || app.getIsDelete() == 1) {
            throw new CrmChatException("Application does not exist");
        }

        // 软删除
        app.setIsDelete(1);
        boolean success = applicationMapper.updateById(app) > 0;
        if (!success) {
            throw new CrmChatException("Failed to delete");
        }

        return "Deleted successfully";
    }

    /**
     * 重置Token
     * PHP Reference: Application.php::reset()
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> resetToken(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ApplicationEntity app = applicationMapper.selectById(id);
        if (app == null || app.getIsDelete() == 1) {
            throw new CrmChatException("Application does not exist");
        }

        // 生成新凭证
        Random random = new Random();
        int rand = 1000 + random.nextInt(9000); // 1000-9999
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        String appSecret = md5(app.getAppid() + timestamp + rand);

        Map<String, Object> params = new HashMap<>();
        params.put("appid", app.getAppid());
        params.put("app_secret", appSecret);
        params.put("rand", rand);
        params.put("timestamp", timestamp);

        // 生成新token
        String tokenJson = JsonUtils.toJsonString(params);
        String token = Base64.getEncoder().encodeToString(tokenJson.getBytes(StandardCharsets.UTF_8));
        String tokenMd5 = md5(token);

        // 更新应用凭证
        app.setRand(rand);
        app.setTimestamp(timestamp);
        app.setAppSecret(appSecret);
        app.setToken(token);
        app.setTokenMd5(tokenMd5);

        boolean success = applicationMapper.updateById(app) > 0;
        if (!success) {
            throw new CrmChatException("Failed to reset");
        }

        // 返回新凭证信息
        Map<String, Object> result = new HashMap<>();
        result.put("rand", rand);
        result.put("timestamp", timestamp);
        result.put("app_secret", appSecret);
        result.put("token", token);
        result.put("token_md5", tokenMd5);
        return result;
    }
    public static void main(String[] args) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", "202116257358989495");
        params.put("app_secret", "28242c7066e9166b46f9b41c10e18d72");
        params.put("rand", 9718);
        params.put("timestamp", 1757126462);

        // 生成新token
        String tokenJson = JsonUtils.toJsonString(params);
        String token = Base64.getEncoder().encodeToString(tokenJson.getBytes(StandardCharsets.UTF_8));
        System.out.println(token);
        String tokenMd5 = md5(token);

        System.out.println(tokenMd5);
        String tokenJson1 = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        // 解析JSON格式的应用信息
        Map<String, Object> appInfo = JsonUtils.parseObject(tokenJson1,Map.class);
        System.out.println(appInfo);
    }

    /**
     * 解析Token - 根据tokenMd5值解析并返回应用信息
     * 参考PHP: ApplicationServices.php::parseToken()
     * @param tokenMd5 token的MD5值（32位）或完整token
     * @param additionalInfo 额外信息映射，用于创建用户信息
     * @return 包含应用信息的Map
     */
    public Map<String, Object> parseToken(String tokenMd5, Map<String, Object> additionalInfo) {
        if (tokenMd5 == null || tokenMd5.trim().isEmpty()) {
           return null;
        }

        String token;
        // 如果是32位MD5值，先查找对应的完整token
        if (tokenMd5.trim().length() == 32) {
            QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("token_md5", tokenMd5.trim());
            wrapper.eq("is_delete", 0);

            ApplicationEntity app = applicationMapper.selectOne(wrapper);
            if (app == null) {
                throw new CrmChatException("Invalid token or application not found");
            }
            token = app.getToken();
        } else {
            token = tokenMd5.trim();
        }

        try {
            // 解析Base64编码的token JSON（模拟PHP的解密过程）
            String tokenJson = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            // 解析JSON格式的应用信息
            Map<String, Object> appInfo = JsonUtils.parseObject(tokenJson,Map.class);

            if (!appInfo.containsKey("appid")) {
                throw new CrmChatException("Missing application ID in token");
            }

            String appid = (String) appInfo.get("appid");

            // 根据appid查找应用数据
            QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            wrapper.eq("is_delete", 0);

            ApplicationEntity appData = applicationMapper.selectOne(wrapper);
            if (appData == null) {
                throw new CrmChatException("Application not found");
            }

            // 验证app_secret
            String expectedSecret = md5(appData.getAppid() + appData.getTimestamp() + appData.getRand());
            String actualSecret = (String) appInfo.get("app_secret");

            if (!expectedSecret.equals(actualSecret)) {
                throw new CrmChatException("Invalid app_secret value");
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("appInfo", appInfo);

            // 如果提供了额外信息，创建用户信息（参考PHP逻辑）
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                // 注意：这里需要在Java中实现用户创建逻辑
                // 由于没有对应的用户服务，这里只做简单映射
                result.put("user", additionalInfo);
            }

            return result;

        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * MD5加密工具方法
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 encryption failed", e);
        }
    }
}
