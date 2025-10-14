package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.common.utils.JsonUtils;
import io.renren.crmchat.CrmChatApplication;
import io.renren.crmchat.dao.ApplicationMapper;
import io.renren.crmchat.entity.ApplicationEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
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
    private final FormBuilder formBuilder;

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
        response.put("count", (int) result.getTotal());
        return response;
    }

    /**
     * 获取表单规则
     * 对应PHP: ApplicationServices.php::getFormRule()
     *
     * PHP代码:
     * return [
     *     FormBuilder::frameImage('icon', '应用图标', $this->url('admin/widget.images/index', ['fodder' => 'icon'], true), $data['value'])
     *         ->icon('ios-image')->width('950px')->height('420px')->info($data['desc'])->col(13)->required(),
     *     FormBuilder::input('name', '应用名称', $data['name'] ?? '')->required(),
     *     FormBuilder::textarea('introduce', '应用简介', $data['introduce'] ?? ''),
     * ];
     */
    public List<BaseComponent> getFormRule(Map<String, Object> data) {
        List<BaseComponent> rules = new ArrayList<>();

        // 1. Application icon (FrameImage component)
        rules.add(formBuilder.frameImage("icon", "Application Icon",
                "/admin/widget/images/index?fodder=icon",
                (String) data.getOrDefault("icon", ""))
            .icon("ios-image")
            .width("950px")
            .height("420px")
            .col(13)
            .required());

        // 2. Application name
        rules.add(formBuilder.input("name", "Application Name",
                (String) data.getOrDefault("name", ""))
            .required());

        // 3. Application summary
        rules.add(formBuilder.textarea("introduce", "Application Description",
                (String) data.getOrDefault("introduce", "")));

        return rules;
    }

    /**
     * 获取创建表单
     * 对应PHP: ApplicationServices.php::getCreateForm()
     *
     * PHP代码:
     * public function getCreateForm()
     * {
     *     return create_form('添加应用', $this->getFormRule(), $this->url('admin/app'), 'post');
     * }
     */
    public Map<String, Object> getCreateForm() {
        return FormHelper.createForm(
            "Add Application",
            getFormRule(new HashMap<>()),
            "/admin/app",
            "POST"
        );
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
     * 对应PHP: ApplicationServices.php::getUpdateForm()
     *
     * PHP代码:
     * public function getUpdateForm(int $id)
     * {
     *     $appInfo = $this->dao->get($id);
     *     if (!$appInfo) {
     *         throw new AdminException('修改的应用不存在');
     *     }
     *     return create_form('修改应用', $this->getFormRule($appInfo->toArray()), $this->url('admin/app', ['id' => $id]), 'put');
     * }
     */
    public Map<String, Object> getEditForm(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ApplicationEntity app = applicationMapper.selectById(id);
        if (app == null || app.getIsDelete() == 1) {
            throw new CrmChatException("Application does not exist");
        }

        // 转换实体为Map（对应PHP的toArray()）
        Map<String, Object> appData = new HashMap<>();
        appData.put("icon", app.getIcon());
        appData.put("name", app.getName());
        appData.put("introduce", app.getIntroduce());

        return FormHelper.createForm(
            "Edit Application",
            getFormRule(appData),
            "/admin/app/" + id,
            "PUT"
        );
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
     * 生成应用信息（用于租户创建时）
     * PHP Reference: ApplicationServices.php::generateAppInfo()
     *
     * @return Map包含: appid, app_secret, rand, timestamp
     */
    public Map<String, Object> generateAppInfo() {
        Random random = new Random();
        int rand = 1000 + random.nextInt(9000); // 1000-9999
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        String appid = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + timestamp + rand;
        String appSecret = md5(appid + timestamp + rand);

        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("appid", appid);
        appInfo.put("app_secret", appSecret);
        appInfo.put("rand", rand);
        appInfo.put("timestamp", timestamp);

        return appInfo;
    }

    /**
     * 为租户创建应用数据
     * PHP Reference: TenantServices.php::createTenantApplication()
     *
     * @param tenantName 租户名称
     * @param appInfo 应用信息（包含appid, app_secret, rand, timestamp）
     * @return 创建是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createTenantApplication(String tenantName, Map<String, Object> appInfo) {
        try {
            String appid = (String) appInfo.get("appid");
            String appSecret = (String) appInfo.get("app_secret");
            Integer rand = (Integer) appInfo.get("rand");
            Integer timestamp = (Integer) appInfo.get("timestamp");

            // 检查应用是否已存在
            QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            wrapper.eq("is_delete", 0);
            ApplicationEntity existingApp = applicationMapper.selectOne(wrapper);

            if (existingApp != null) {
                // 应用已存在，无需重复创建
                System.out.println("Application already exists, appid: " + appid);
                return true;
            }

            // 生成token（使用JsonUtils保持与resetToken一致）
            String tokenJson = JsonUtils.toJsonString(appInfo);
            String token = Base64.getEncoder().encodeToString(tokenJson.getBytes(StandardCharsets.UTF_8));
            String tokenMd5 = md5(token);

            // 准备应用数据
            ApplicationEntity application = new ApplicationEntity();
            application.setAppid(appid);
            application.setName(tenantName + " Support Application");
            application.setIcon(""); // default icon placeholder
            application.setIntroduce("Support application automatically created for tenant " + tenantName);
            application.setAppSecret(appSecret);
            application.setTimestamp(timestamp);
            application.setRand(rand);
            application.setToken(token);
            application.setTokenMd5(tokenMd5);
            application.setIsDelete(0);

            // 保存到application表
            return applicationMapper.insert(application) > 0;
        } catch (Exception e) {
            System.err.println("Failed to create tenant application: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据appid获取应用信息
     *
     * @param appid 应用ID
     * @return 应用信息Map,如果不存在返回null
     */
    public Map<String, Object> getApplicationByAppid(String appid) {
        if (appid == null || appid.trim().isEmpty()) {
            return null;
        }

        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("is_delete", 0);

        ApplicationEntity app = applicationMapper.selectOne(wrapper);
        if (app == null) {
            return null;
        }

        // 转换为Map返回
        Map<String, Object> result = new HashMap<>();
        result.put("id", app.getId());
        result.put("appid", app.getAppid());
        result.put("name", app.getName());
        result.put("icon", app.getIcon());
        result.put("introduce", app.getIntroduce());
        result.put("app_secret", app.getAppSecret());
        result.put("rand", app.getRand());
        result.put("timestamp", app.getTimestamp());
        result.put("token", app.getToken());
        result.put("token_md5", app.getTokenMd5());

        return result;
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
