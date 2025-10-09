package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ApplicationMapper;
import io.renren.crmchat.entity.ApplicationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Calendar;
import java.util.Map;
import java.util.Random;

/**
 * Tenant 应用管理服务
 * PHP Reference: /app/controller/tenant/Application.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getApplication(): 获取应用信息
 *    - 根据appid查询
 * 2. createApplication(): 创建应用
 *    - 验证必填字段
 *    - 生成appid、app_secret、token
 *    - 检查名称唯一性
 * 3. updateApplication(): 更新应用
 *    - 验证必填字段
 *    - 更新基本信息
 * 4. deleteApplication(): 删除应用
 *    - 软删除（is_delete=1）
 * 5. resetToken(): 重置token
 *    - 重新生成app_secret和token
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantApplicationService {

    private final ApplicationMapper applicationMapper;

    /**
     * 获取应用信息
     * GET /api/tenant/app
     *
     * PHP Reference: Application.php::index()
     *
     * 业务逻辑:
     * 1. 根据appid查询应用
     * 2. 返回应用信息
     *
     * @param appid 租户appid
     * @return 应用信息
     */
    public ApplicationEntity getApplication(String appid) {
        // PHP: $where["appid"] = $appid;
        // PHP: return $this->success("查询成功",$this->services->getOne($where)->toArray());

        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("is_delete", 0);

        ApplicationEntity application = applicationMapper.selectOne(wrapper);
        if (application == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        return application;
    }

    /**
     * 创建应用
     * POST /api/tenant/app
     *
     * PHP Reference: Application.php::save()
     *
     * 业务逻辑:
     * 1. 验证icon和name非空
     * 2. 检查name唯一性
     * 3. 生成appid（年份+时间戳+随机数）
     * 4. 生成app_secret和token
     * 5. 保存到数据库
     *
     * @param data 应用数据（icon, name, introduce）
     * @return 新创建的应用信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ApplicationEntity createApplication(Map<String, Object> data) {
        // 1. PHP: if (!$data['icon']) return $this->fail('请选择应用图标');
        if (!data.containsKey("icon") || data.get("icon") == null || data.get("icon").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select application icon");
        }

        // 2. PHP: if (!$data['name']) return $this->fail('请填写应用名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter application name");
        }

        String name = data.get("name").toString();

        // 3. PHP: if ($this->services->count(['name' => $data['name']])) return $this->fail('应用名称已存在');
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        wrapper.eq("is_delete", 0);
        Long count = applicationMapper.selectCount(wrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Application name already exists");
        }

        // 4. PHP: 生成appid和token
        Random random = new Random();
        int rand = random.nextInt(9000) + 1000; // 1000-9999
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String appid = year + String.valueOf(timestamp) + rand;
        String appSecret = DigestUtils.md5DigestAsHex((appid + timestamp + rand).getBytes());

        // 简化版token生成（PHP使用Encrypter，这里使用MD5）
        String token = DigestUtils.md5DigestAsHex((appid + appSecret + rand + timestamp).getBytes());
        String tokenMd5 = DigestUtils.md5DigestAsHex(token.getBytes());

        // 5. 创建应用记录
        ApplicationEntity application = new ApplicationEntity();
        application.setAppid(appid);
        application.setIcon(data.get("icon").toString());
        application.setName(name);

        if (data.containsKey("introduce") && data.get("introduce") != null) {
            application.setIntroduce(data.get("introduce").toString());
        }

        application.setRand(rand);
        application.setTimestamp(timestamp);
        application.setAppSecret(appSecret);
        application.setToken(token);
        application.setTokenMd5(tokenMd5);
        application.setIsDelete(0);

        int result = applicationMapper.insert(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save");
        }

        return application;
    }

    /**
     * 更新应用
     * PUT /api/tenant/app/:id
     *
     * PHP Reference: Application.php::update()
     *
     * 业务逻辑:
     * 1. 验证icon和name非空
     * 2. 更新应用信息
     *
     * @param id   应用ID
     * @param data 应用数据（icon, name, introduce）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateApplication(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['icon']) return $this->fail('请选择应用图标');
        if (!data.containsKey("icon") || data.get("icon") == null || data.get("icon").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select application icon");
        }

        // 2. PHP: if (!$data['name']) return $this->fail('请填写应用名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter application name");
        }

        // 查询应用是否存在
        ApplicationEntity application = applicationMapper.selectById(id);
        if (application == null || application.getIsDelete() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        // 3. PHP: $this->services->update($id, $data);
        application.setIcon(data.get("icon").toString());
        application.setName(data.get("name").toString());

        if (data.containsKey("introduce") && data.get("introduce") != null) {
            application.setIntroduce(data.get("introduce").toString());
        }

        int result = applicationMapper.updateById(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save");
        }
    }

    /**
     * 删除应用
     * DELETE /api/tenant/app/:id
     *
     * PHP Reference: Application.php::delete()
     *
     * 业务逻辑:
     * 1. 软删除应用（is_delete=1）
     *
     * @param id 应用ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Integer id) {
        // PHP: $this->services->update($id, ['is_delete' => 1]);

        ApplicationEntity application = applicationMapper.selectById(id);
        if (application == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        application.setIsDelete(1);

        int result = applicationMapper.updateById(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }

    /**
     * 重置token
     * PUT /api/tenant/app/reset/:id
     *
     * PHP Reference: Application.php::reset()
     *
     * 业务逻辑:
     * 1. 验证应用存在
     * 2. 重新生成rand、timestamp、app_secret、token
     * 3. 更新到数据库
     * 4. 返回新的token信息
     *
     * @param id 应用ID
     * @return 新的token信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> resetToken(Integer id) {
        // 1. PHP: $appInfo = $this->services->get($id);
        ApplicationEntity application = applicationMapper.selectById(id);
        if (application == null || application.getIsDelete() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Application does not exist");
        }

        // 2. PHP: 重新生成token相关字段
        Random random = new Random();
        int rand = random.nextInt(9000) + 1000;
        int timestamp = (int) (System.currentTimeMillis() / 1000);

        String appSecret = DigestUtils.md5DigestAsHex((application.getAppid() + timestamp + rand).getBytes());
        String token = DigestUtils.md5DigestAsHex((application.getAppid() + appSecret + rand + timestamp).getBytes());
        String tokenMd5 = DigestUtils.md5DigestAsHex(token.getBytes());

        // 3. 更新到数据库
        application.setRand(rand);
        application.setTimestamp(timestamp);
        application.setAppSecret(appSecret);
        application.setToken(token);
        application.setTokenMd5(tokenMd5);

        int result = applicationMapper.updateById(application);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to reset");
        }

        // 4. 返回新的token信息
        return Map.of(
                "rand", rand,
                "timestamp", timestamp,
                "app_secret", appSecret,
                "token", token,
                "token_md5", tokenMd5
        );
    }
}
