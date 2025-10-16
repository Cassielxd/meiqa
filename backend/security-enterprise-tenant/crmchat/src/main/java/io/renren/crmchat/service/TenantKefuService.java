package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceGroupMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.dao.TenantsMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceGroupEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.entity.TenantsEntity;
import io.renren.crmchat.security.TenantGuard;
import io.renren.crmchat.security.TenantSecurityUtils;
import io.renren.crmchat.service.common.PasswordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Tenant 客服管理服务
 * PHP Reference: /app/controller/tenant/Service.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. updateKefu(): 更新客服信息
 *    - 验证appid匹配（多租户隔离）
 *    - 可选密码修改（password + true_password验证）
 *    - 过滤空值
 *    - BCrypt密码加密
 * 2. deleteKefu(): 删除客服
 *    - 验证appid匹配
 *    - 硬删除（与PHP一致）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantKefuService {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,30}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9a-z_$]{6,20}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceGroupMapper chatServiceGroupMapper;
    private final ChatUserMapper chatUserMapper;
    private final PasswordService passwordService;
    private final TenantsMapper tenantsMapper;
    private final io.renren.crmchat.service.common.TokenService tokenService;
    private final io.renren.crmchat.formbuilder.FormBuilder formBuilder;

    /**
     * 更新客服
     * PHP Reference: Service.php::update()
     *
     * 业务逻辑:
     * 1. 验证客服存在
     * 2. 验证appid匹配（安全：防止跨租户操作）
     * 3. 可选密码修改（需要password和true_password一致）
     * 4. 过滤空值字段
     * 5. 更新数据库
     *
     * @param id 客服ID
     * @param data 更新数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateKefu(Integer id, Map<String, Object> data) {
        ChatServiceEntity kefu = chatServiceMapper.selectById(id);
        if (kefu == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(kefu.getAppid(), "Customer service agent does not exist");

        String appid = kefu.getAppid();

        if (data.containsKey("nickname")) {
            String nickname = stringValue(data.get("nickname"));
            if (nickname.isEmpty()) {
                throw new io.renren.crmchat.exception.CrmChatException("Customer service name cannot be empty");
            }
            kefu.setNickname(nickname);
        }

        if (data.containsKey("avatar")) {
            String avatar = stringValue(data.get("avatar"));
            if (avatar.isEmpty()) {
                throw new io.renren.crmchat.exception.CrmChatException("Please upload customer service avatar");
            }
            kefu.setAvatar(avatar);
        }

        if (data.containsKey("account")) {
            String account = stringValue(data.get("account"));
            if (!account.isEmpty() && !account.equals(kefu.getAccount())) {
                if (!ACCOUNT_PATTERN.matcher(account).matches()) {
                    throw new io.renren.crmchat.exception.CrmChatException("Account must be 4-30 alphanumeric characters");
                }
                QueryWrapper<ChatServiceEntity> accountWrapper = new QueryWrapper<>();
                accountWrapper.eq("appid", appid).eq("account", account).ne("id", id);
                if (chatServiceMapper.selectCount(accountWrapper) > 0) {
                    throw new io.renren.crmchat.exception.CrmChatException("This customer service account already exists");
                }
                kefu.setAccount(account);
            }
        }

        if (data.containsKey("phone")) {
            String phone = stringValue(data.get("phone"));
            if (!phone.isEmpty()) {
                validatePhone(phone);
                ensurePhoneUniqueForTenant(appid, phone, id);
                kefu.setPhone(phone);
            }
        }

        if (data.containsKey("group_id")) {
            String groupIdStr = stringValue(data.get("group_id"));
            if (!groupIdStr.isEmpty()) {
                try {
                    kefu.setGroupId(Integer.parseInt(groupIdStr));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (data.containsKey("status")) {
            kefu.setStatus(parseFlag(data.get("status"), kefu.getStatus() == null ? 1 : kefu.getStatus()));
        }

        if (data.containsKey("welcome_words") && data.get("welcome_words") != null) {
            kefu.setWelcomeWords(data.get("welcome_words").toString());
        }

        if (data.containsKey("auto_reply")) {
            kefu.setAutoReply(parseFlag(data.get("auto_reply"), kefu.getAutoReply() == null ? 0 : kefu.getAutoReply()));
        }

        if (data.containsKey("notify")) {
            kefu.setNotify(parseFlag(data.get("notify"), kefu.getNotify() == null ? 1 : kefu.getNotify()));
        }

        if (data.containsKey("customer")) {
            kefu.setCustomer(parseFlag(data.get("customer"), kefu.getCustomer() == null ? 1 : kefu.getCustomer()));
        }

        if (data.containsKey("password")) {
            String password = stringValue(data.get("password"));
            if (!password.isEmpty()) {
                if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    throw new io.renren.crmchat.exception.CrmChatException("Password must be 6-20 alphanumeric characters");
                }
                String truePassword = stringValue(data.get("true_password"));
                if (!truePassword.isEmpty() && !password.equals(truePassword)) {
                    throw new io.renren.crmchat.exception.CrmChatException("Passwords do not match");
                }
                kefu.setPassword(passwordService.hash(password));
            }
        }

        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Update failed, please try again later");
        }

        syncChatUserAfterUpdate(kefu);
    }

    /**
     * 删除客服
     * PHP Reference: Service.php::delete()
     *
     * 业务逻辑:
     * 1. 验证客服存在
     * 2. 验证appid匹配（安全：防止跨租户操作）
     * 3. 删除客服（硬删除）
     *
     * @param id 客服ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKefu(Integer id) {
        // 1. PHP: $serviceInfo = $this->services->get($id);
        ChatServiceEntity serviceInfo = chatServiceMapper.selectById(id);
        if (serviceInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(serviceInfo.getAppid(), "Customer service agent does not exist");

        // 3. PHP: $this->services->delete($id);
        int result = chatServiceMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Delete failed, please try again later");
        }
    }

    /**
     * 更新客服状态
     * PHP Reference: Service.php::updateStatus()
     *
     * 业务逻辑:
     * 1. 验证客服存在
     * 2. 验证appid匹配（安全：防止跨租户操作）
     * 3. 更新status字段
     *
     * @param id 客服ID
     * @param status 状态值（0-禁用，1-启用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateKefuStatus(Integer id, Integer status) {
        // 1. PHP: $serviceInfo = $this->services->get($id);
        ChatServiceEntity serviceInfo = chatServiceMapper.selectById(id);
        if (serviceInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(serviceInfo.getAppid(), "Customer service agent does not exist");

        // 3. PHP: $this->services->update($id, ['status' => $status]);
        ChatServiceEntity updateEntity = new ChatServiceEntity();
        updateEntity.setId(id);
        updateEntity.setStatus(status);

        int result = chatServiceMapper.updateById(updateEntity);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Status update failed");
        }
    }

    /**
     * 获取客服分组列表
     * GET /api/tenant/kefu/groups
     *
     * PHP Reference: Service.php::groups()
     *
     * 业务逻辑:
     * 1. 验证租户身份
     * 2. 查询当前租户的所有客服分组
     * 3. 按sort排序
     *
     * @return 客服分组列表
     */
    public List<ChatServiceGroupEntity> getKefuGroups() {
        // PHP: $list = $groupServices->getGroupList(['appid' => $tenantInfo['appid']]);
        // getDataList($where, ['*'], 'sort')

        QueryWrapper<ChatServiceGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort");
        return chatServiceGroupMapper.selectList(wrapper);
    }

    /**
     * 创建客服分组
     * POST /api/tenant/kefu/group
     *
     * PHP Reference: ServiceGroup.php::save($id=null)
     *
     * 业务逻辑:
     * 1. 验证name不为空（必填）
     * 2. 设置appid（多租户隔离）
     * 3. 插入新记录
     *
     * @param data 分组数据（name, sort）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createKefuGroup(Map<String, Object> data) {
        // 1. PHP: if (!$data['name'])
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing group name");
        }

        // 2. PHP: $appid = $this->request->tenantAppid();
        //         $data["appid"] = $appid;
        ChatServiceGroupEntity groupEntity = new ChatServiceGroupEntity();
        groupEntity.setName(data.get("name").toString());

        // 3. PHP: $this->services->save($data);
        if (data.containsKey("sort") && data.get("sort") != null && !data.get("sort").toString().isEmpty()) {
            groupEntity.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            groupEntity.setSort(0); // Default sort value
        }

        int result = chatServiceGroupMapper.insert(groupEntity);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add");
        }
    }

    /**
     * 更新客服分组
     * PUT /api/tenant/kefu/group/:id
     *
     * PHP Reference: ServiceGroup.php::save($id)
     *
     * 业务逻辑:
     * 1. 验证name不为空（必填）
     * 2. 设置appid（多租户隔离）
     * 3. 更新记录
     *
     * @param id 分组ID
     * @param data 分组数据（name, sort）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateKefuGroup(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['name'])
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing group name");
        }

        // Verify group exists and belongs to current tenant (security)
        ChatServiceGroupEntity groupInfo = chatServiceGroupMapper.selectById(id);
        if (groupInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Group does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(groupInfo.getAppid(), "Group does not exist");

        // 2. PHP: $appid = $this->request->tenantAppid();
        //         $data["appid"] = $appid;
        //         $this->services->update($id, $data);
        groupInfo.setName(data.get("name").toString());

        if (data.containsKey("sort") && data.get("sort") != null && !data.get("sort").toString().isEmpty()) {
            groupInfo.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        int result = chatServiceGroupMapper.updateById(groupInfo);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Update failed");
        }
    }

    /**
     * 删除客服分组
     * DELETE /api/tenant/kefu/group/:id
     *
     * PHP Reference: ServiceGroup.php::delete($id)
     *
     * 业务逻辑:
     * 1. 验证分组存在且属于当前租户
     * 2. 检查是否有客服关联此分组
     * 3. 如果有关联，返回错误"Please remove customer service agent association first"
     * 4. 删除分组
     *
     * @param id 分组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKefuGroup(Integer id) {
        // 1. Verify group exists and belongs to current tenant (security)
        ChatServiceGroupEntity groupInfo = chatServiceGroupMapper.selectById(id);
        if (groupInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Group does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(groupInfo.getAppid(), "Group does not exist");

        // 2. PHP: if ($services->count(['group_id' => $id]))
        //         return $this->fail('请先解除客服关联');
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", id);
        Long count = chatServiceMapper.selectCount(wrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Please remove customer service associations first");
        }

        // 3. PHP: $this->services->delete($id);
        int result = chatServiceGroupMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Delete failed");
        }
    }

    /**
     * 获取客服分组详情
     * GET /api/tenant/kefu/group/:id
     *
     * PHP Reference: ServiceGroup.php::create($id) 或通用的get方法
     *
     * 业务逻辑:
     * 1. 验证分组存在且属于当前租户
     * 2. 返回分组详情
     *
     * @param id 分组ID
     * @return 分组详情
     */
    public ChatServiceGroupEntity getKefuGroupDetail(Integer id) {
        ChatServiceGroupEntity groupInfo = chatServiceGroupMapper.selectById(id);

        if (groupInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Group does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(groupInfo.getAppid(), "Group does not exist");

        return groupInfo;
    }

    /**
     * 获取客服列表
     * GET /api/tenant/kefu/list
     *
     * PHP Reference: Service.php::list()
     *
     * 业务逻辑:
     * 1. 接收过滤参数（nickname, status, group_id）
     * 2. 验证租户身份
     * 3. 强制添加appid过滤（多租户隔离）
     * 4. 返回客服列表
     *
     * @param filters 过滤条件（nickname, status, group_id）
     * @return 客服列表
     */
    public List<ChatServiceEntity> getKefuList(Map<String, Object> filters) {
        // PHP: $where = $this->request->getMore([...]);
        // PHP: $where['appid'] = $tenantInfo['appid'];

        QueryWrapper<ChatServiceEntity> wrapper = buildKefuQueryWrapper(filters);

        // PHP: return $this->success($this->services->getServiceList($where));
        return chatServiceMapper.selectList(wrapper);
    }

    /**
     * 获取客服数量
     * PHP: $count = $this->dao->count($where);
     */
    public long getKefuCount(Map<String, Object> filters) {
        QueryWrapper<ChatServiceEntity> wrapper = buildKefuQueryWrapper(filters);
        return chatServiceMapper.selectCount(wrapper);
    }

    /**
     * 构建客服查询条件（复用逻辑）
     */
    private QueryWrapper<ChatServiceEntity> buildKefuQueryWrapper(Map<String, Object> filters) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();

        // 可选过滤条件
        if (filters.containsKey("nickname") && filters.get("nickname") != null && !filters.get("nickname").toString().trim().isEmpty()) {
            wrapper.like("nickname", filters.get("nickname").toString());
        }

        if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().isEmpty()) {
            wrapper.eq("status", Integer.parseInt(filters.get("status").toString()));
        }

        if (filters.containsKey("group_id") && filters.get("group_id") != null && !filters.get("group_id").toString().isEmpty()) {
            wrapper.eq("group_id", Integer.parseInt(filters.get("group_id").toString()));
        }

        return wrapper;
    }

    /**
     * 获取客服详情
     * GET /api/tenant/kefu/:id
     *
     * PHP Reference: Service.php::read()
     *
     * 业务逻辑:
     * 1. 验证id参数
     * 2. 验证客服存在
     * 3. 验证appid匹配（安全：防止跨租户访问）
     * 4. 统一返回"Customer service agent does not exist"（避免ID遍历攻击）
     *
     * @param id 客服ID
     * @return 客服详情
     */
    public ChatServiceEntity getKefuDetail(Integer id) {
        // PHP: $info = $this->services->get($id);
        ChatServiceEntity serviceInfo = chatServiceMapper.selectById(id);
        if (serviceInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(serviceInfo.getAppid(), "Customer service agent does not exist");

        return serviceInfo;
    }

    /**
     * 创建客服
     * POST /api/tenant/kefu/create
     *
     * PHP Reference: Service.php::save()
     *
     * 业务逻辑:
     * 1. 验证密码匹配（password === true_password）
     * 2. 验证必填字段（nickname, account, password）
     * 3. 检查账号唯一性（在当前租户下）
     * 4. 检查客服数量限制（currentCount >= max_services）
     * 5. 设置appid（多租户隔离）
     * 6. BCrypt密码加密
     * 7. 保存并返回ID
     *
     * @param data 客服数据
     * @return 新创建的客服ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createKefu(Map<String, Object> data) {
        validateCreateParams(data);

        String password = stringValue(data.get("password"));
        String truePassword = stringValue(data.get("true_password"));
        if (!truePassword.isEmpty() && !password.equals(truePassword)) {
            throw new io.renren.crmchat.exception.CrmChatException("Passwords do not match");
        }

        String appid = TenantSecurityUtils.requireAppid();

        QueryWrapper<ChatServiceEntity> accountWrapper = new QueryWrapper<>();
        accountWrapper.eq("account", data.get("account").toString());
        accountWrapper.eq("appid", appid);
        Long accountCount = chatServiceMapper.selectCount(accountWrapper);

        if (accountCount > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("This customer service account already exists");
        }

        // 4. PHP: $currentCount = $this->services->count(['appid' => $tenantInfo['appid']]);
        //         if ($currentCount >= $tenantInfo['max_services'])
        QueryWrapper<ChatServiceEntity> countWrapper = new QueryWrapper<>();
        countWrapper.eq("appid", appid);
        Long currentCount = chatServiceMapper.selectCount(countWrapper);

        // 查询租户的max_services限制
        QueryWrapper<TenantsEntity> tenantWrapper = new QueryWrapper<>();
        tenantWrapper.eq("appid", appid);
        TenantsEntity tenant = tenantsMapper.selectOne(tenantWrapper);

        if (tenant == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Tenant does not exist");
        }

        if (currentCount >= tenant.getMaxServices()) {
            throw new io.renren.crmchat.exception.CrmChatException("Customer service count exceeds limit");
        }

        // 5. PHP: $data['appid'] = $tenantInfo['appid'];
        //         $data['password'] = password_hash($data['password'], PASSWORD_DEFAULT);
        String phone = stringValue(data.get("phone"));
        ensurePhoneUniqueForTenant(appid, phone, null);

        ChatServiceEntity newKefu = new ChatServiceEntity();
        newKefu.setAppid(appid);
        newKefu.setNickname(data.get("nickname").toString());
        newKefu.setAccount(data.get("account").toString());
        newKefu.setPassword(passwordService.hash(password));
        newKefu.setPhone(phone);
        newKefu.setAvatar(data.get("avatar").toString());
        newKefu.setGroupId(parseIntOrDefault(data.get("group_id"), 0));
        newKefu.setStatus(parseFlag(data.get("status"), 0));
        newKefu.setWelcomeWords(data.getOrDefault("welcome_words", "").toString());
        newKefu.setAutoReply(parseFlag(data.get("auto_reply"), 0));
        newKefu.setNotify(parseFlag(data.get("notify"), 1));
        newKefu.setCustomer(parseFlag(data.get("customer"), 1));
        newKefu.setOnline(0);
        newKefu.setIsApp(0);
        newKefu.setIsBackstage(0);
        // PHP uses empty string initially, or md5(time().uniqid()) which is 32 chars
        // Remove hyphens from UUID to fit varchar(35): 36 chars -> 32 chars
        newKefu.setUniqid(java.util.UUID.randomUUID().toString().replace("-", ""));

        int currentTime = (int) (System.currentTimeMillis() / 1000);
        newKefu.setAddTime(currentTime);
        newKefu.setUpdateTime(currentTime);

        int result = chatServiceMapper.insert(newKefu);
        if (result <= 0 || newKefu.getId() == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add customer service representative, please try again later");
        }

        Integer userId = ensureChatUserAssociation(appid, phone, newKefu.getNickname(), newKefu.getAvatar());
        if (userId != null) {
            newKefu.setUserId(userId);
            newKefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
            chatServiceMapper.updateById(newKefu);
        }

        return newKefu.getId();
    }

    /**
     * 获取创建客服表单配置
     * PHP Reference: ChatServiceServices.php::createKefuForTent()
     *
     * @return 表单配置
     */
    public Map<String, Object> getCreateFormConfig() {
        // 获取当前租户appid
        String tenantAppid = io.renren.crmchat.security.UserContext.getAppid();

        // 获取客服分组选项
        List<io.renren.crmchat.formbuilder.components.OptionComponent> groupOptions = getGroupOptions(tenantAppid);

        // 构建表单字段（一比一还原PHP createServiceFormForTent方法）
        List<io.renren.crmchat.formbuilder.components.BaseComponent> field = new java.util.ArrayList<>();

        // PHP: $field[] = $this->builder->select('group_id', '请选择分组', $formData['group_id'] ?? 0)->options($seervice->getOptions());
        field.add(formBuilder.select("group_id", "请选择分组", 0)
            .options(groupOptions));

        // PHP: $field[] = $this->builder->frameImage('avatar', '客服头像', $this->url('tenant/widget.images/index', ['fodder' => 'avatar'], true), $formData['avatar'] ?? '')->icon('ios-add')->width('950px')->height('420px');
        field.add(formBuilder.frameImage("avatar", "客服头像", "tenant/#/tenant/widget.images/index.html?fodder=avatar", "")
            .icon("ios-add")
            .width("950px")
            .height("420px"));

        // PHP: $field[] = $this->builder->i
        // nput('nickname', '客服名称', $formData['nickname'] ?? '')->col(24)->required();
        field.add(formBuilder.input("nickname", "客服名称", "")
            .col(24)
            .required());

        // PHP: $field[] = $this->builder->input('phone', '手机号码', $formData['phone'] ?? '')->col(24)->required();
        field.add(formBuilder.input("phone", "手机号码", "")
            .col(24)
            .required());

        // PHP: else { $field[] = $this->builder->input('account', '登录账号')->col(24)->required(); }
        field.add(formBuilder.input("account", "登录账号", "")
            .col(24)
            .required());

        // PHP: $field[] = $this->builder->input('password', '登录密码')->type('password')->col(24)->required();
        field.add(formBuilder.input("password", "登录密码", "")
            .type("password")
            .col(24)
            .required());

        // PHP: $field[] = $this->builder->input('true_password', '确认密码')->type('password')->col(24)->required();
        field.add(formBuilder.input("true_password", "确认密码", "")
            .type("password")
            .col(24)
            .required());

        // PHP: $field[] = $this->builder->textarea('welcome_words', '欢迎语', $formData['welcome_words'] ?? '');
        field.add(formBuilder.textarea("welcome_words", "欢迎语", ""));

        // PHP: $field[] = $this->builder->switches('auto_reply', '自动回复', (int)($formData['auto_reply'] ?? 0))->falseValue(0)->trueValue(1)->openStr('打开')->closeStr('关闭')->size('large');
        field.add(formBuilder.switches("auto_reply", "自动回复", 0)
            .falseValue(0)
            .trueValue(1)
            .openStr("打开")
            .closeStr("关闭")
            .size("large"));

        // PHP: $field[] = $this->builder->switches('status', '客服状态', (int)($formData['status'] ?? 0))->falseValue(0)->trueValue(1)->openStr('打开')->closeStr('关闭')->size('large');
        field.add(formBuilder.switches("status", "客服状态", 0)
            .falseValue(0)
            .trueValue(1)
            .openStr("打开")
            .closeStr("关闭")
            .size("large"));

        // PHP: return create_form('添加客服', $this->createServiceFormForTent(), $this->url('/chat/kefu'), 'POST');
        return io.renren.crmchat.formbuilder.FormHelper.createForm(
            "添加客服",
            field,
            "/chat/kefu",
            "POST"
        );
    }

    /**
     * 获取编辑客服表单配置（带现有数据）
     * PHP: ChatServiceServices::edit() -> createServiceForm($serviceInfo)
     */
    public Map<String, Object> getEditFormConfig(Integer id) {
        // 获取现有客服数据
        ChatServiceEntity kefu = chatServiceMapper.selectById(id);
        if (kefu == null) {
            throw new RuntimeException("Data does not exist");
        }

        // 获取当前租户appid
        String tenantAppid = io.renren.crmchat.security.UserContext.getAppid();

        // 验证客服属于当前租户
        if (!kefu.getAppid().equals(tenantAppid)) {
            throw new RuntimeException("No permission to access this customer service agent");
        }

        // 获取客服分组选项
        List<io.renren.crmchat.formbuilder.components.OptionComponent> groupOptions = getGroupOptions(tenantAppid);

        // 构建表单字段（一比一还原PHP createServiceFormForTent方法，使用现有数据填充）
        List<io.renren.crmchat.formbuilder.components.BaseComponent> field = new java.util.ArrayList<>();

        // PHP: $field[] = $this->builder->select('group_id', '请选择分组', $formData['group_id'] ?? 0)->options($seervice->getOptions());
        field.add(formBuilder.select("group_id", "请选择分组", kefu.getGroupId() != null ? kefu.getGroupId() : 0)
            .options(groupOptions));

        // PHP: $field[] = $this->builder->frameImage('avatar', '客服头像', $this->url('tenant/widget.images/index', ['fodder' => 'avatar'], true), $formData['avatar'] ?? '')->icon('ios-add')->width('950px')->height('420px');
        field.add(formBuilder.frameImage("avatar", "客服头像", "tenant/#/tenant/widget.images/index.html?fodder=avatar", kefu.getAvatar() != null ? kefu.getAvatar() : "")
            .icon("ios-add")
            .width("950px")
            .height("420px"));

        // PHP: $field[] = $this->builder->input('nickname', '客服名称', $formData['nickname'] ?? '')->col(24)->required();
        field.add(formBuilder.input("nickname", "客服名称", kefu.getNickname() != null ? kefu.getNickname() : "")
            .col(24)
            .required());

        // PHP: $field[] = $this->builder->input('phone', '手机号码', $formData['phone'] ?? '')->col(24)->required();
        field.add(formBuilder.input("phone", "手机号码", kefu.getPhone() != null ? kefu.getPhone() : "")
            .col(24)
            .required());

        // PHP: if ($formData) { $field[] = $this->builder->input('account', '登录账号', $formData['account'] ?? '')->col(24)->required(); }
        field.add(formBuilder.input("account", "登录账号", kefu.getAccount() != null ? kefu.getAccount() : "")
            .col(24)
            .required());

        // PHP: $field[] = $this->builder->input('password', '登录密码')->type('password')->col(24);
        field.add(formBuilder.input("password", "登录密码", "")
            .type("password")
            .col(24));

        // PHP: $field[] = $this->builder->input('true_password', '确认密码')->type('password')->col(24);
        field.add(formBuilder.input("true_password", "确认密码", "")
            .type("password")
            .col(24));

        // PHP: $field[] = $this->builder->textarea('welcome_words', '欢迎语', $formData['welcome_words'] ?? '');
        field.add(formBuilder.textarea("welcome_words", "欢迎语", kefu.getWelcomeWords() != null ? kefu.getWelcomeWords() : ""));

        // PHP: $field[] = $this->builder->switches('auto_reply', '自动回复', (int)($formData['auto_reply'] ?? 0))->falseValue(0)->trueValue(1)->openStr('打开')->closeStr('关闭')->size('large');
        field.add(formBuilder.switches("auto_reply", "自动回复", kefu.getAutoReply() != null ? kefu.getAutoReply() : 0)
            .falseValue(0)
            .trueValue(1)
            .openStr("打开")
            .closeStr("关闭")
            .size("large"));

        // PHP: $field[] = $this->builder->switches('status', '客服状态', (int)($formData['status'] ?? 0))->falseValue(0)->trueValue(1)->openStr('打开')->closeStr('关闭')->size('large');
        field.add(formBuilder.switches("status", "客服状态", kefu.getStatus() != null ? kefu.getStatus() : 0)
            .falseValue(0)
            .trueValue(1)
            .openStr("打开")
            .closeStr("关闭")
            .size("large"));

        // PHP: return create_form('编辑客服', $this->createServiceForm($serviceInfo->toArray()), $this->url('/chat/kefu/' . $id), 'PUT');
        return io.renren.crmchat.formbuilder.FormHelper.createForm(
            "编辑客服",
            field,
            "/chat/kefu/" + id,
            "PUT"
        );
    }

    /**
     * 客服登录（管理员代登录）
     * 生成客服登录Token，用于管理员进入客服工作台
     *
     * @param id 客服ID
     * @return {token, exp_time, kefuInfo}
     */
    public java.util.Map<String, Object> kefuLogin(Integer id) {
        // 获取客服信息
        ChatServiceEntity serviceInfo = chatServiceMapper.selectById(id);
        if (serviceInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Logged-in customer service representative does not exist");
        }

        // 验证租户权限
        TenantGuard.ensureOwnedByCurrentTenant(serviceInfo.getAppid(), "Support agent does not exist.");

        // 验证客服账号状态
        if (serviceInfo.getAccount() == null || serviceInfo.getAccount().isEmpty() ||
            serviceInfo.getPassword() == null || serviceInfo.getPassword().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter customer service account and password before accessing the platform");
        }

        if (serviceInfo.getStatus() == null || serviceInfo.getStatus() != 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Customer service account has been disabled");
        }

        // 生成客服Token
        String token = tokenService.generateKefuToken(
            serviceInfo.getId().longValue(),
            serviceInfo.getAccount(),
            serviceInfo.getAppid()
        );

        // 获取Token过期时间
        Long expTime = tokenService.getTokenExpireAt(token);

        // 更新客服状态（设置为在线）
        ChatServiceEntity updateEntity = new ChatServiceEntity();
        updateEntity.setId(serviceInfo.getId());
        updateEntity.setOnline(1);
        updateEntity.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        chatServiceMapper.updateById(updateEntity);

        // 构建返回数据
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("exp_time", expTime);

        // 构建客服信息（移除敏感字段）
        java.util.Map<String, Object> kefuInfo = new java.util.HashMap<>();
        kefuInfo.put("id", serviceInfo.getId());
        kefuInfo.put("account", serviceInfo.getAccount());
        kefuInfo.put("nickname", serviceInfo.getNickname());
        kefuInfo.put("avatar", serviceInfo.getAvatar());
        kefuInfo.put("phone", serviceInfo.getPhone());
        kefuInfo.put("appid", serviceInfo.getAppid());
        kefuInfo.put("group_id", serviceInfo.getGroupId());
        kefuInfo.put("online", 1); // 已设置为在线
        kefuInfo.put("is_app", serviceInfo.getIsApp());
        kefuInfo.put("is_backstage", serviceInfo.getIsBackstage());
        kefuInfo.put("auto_reply", serviceInfo.getAutoReply());
        kefuInfo.put("welcome_words", serviceInfo.getWelcomeWords());
        kefuInfo.put("uniqid", serviceInfo.getUniqid());

        result.put("kefuInfo", kefuInfo);

        return result;
    }

    private void validateCreateParams(Map<String, Object> data) {
        String avatar = stringValue(data.get("avatar"));
        if (avatar.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select customer service avatar");
        }
        data.put("avatar", avatar);

        String nickname = stringValue(data.get("nickname"));
        if (nickname.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please complete required information");
        }
        data.put("nickname", nickname);

        String account = stringValue(data.get("account"));
        if (account.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter account");
        }
        if (!ACCOUNT_PATTERN.matcher(account).matches()) {
            throw new io.renren.crmchat.exception.CrmChatException("Account must be 4-30 alphanumeric characters");
        }
        data.put("account", account);

        String password = stringValue(data.get("password"));
        if (password.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter password");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new io.renren.crmchat.exception.CrmChatException("Password must be 6-20 alphanumeric characters");
        }
        data.put("password", password);

        String phone = stringValue(data.get("phone"));
        if (phone.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter a valid phone number");
        }
        validatePhone(phone);
        data.put("phone", phone);
    }

    private void validatePhone(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter a valid phone number");
        }
    }

    private void ensurePhoneUniqueForTenant(String appid, String phone, Integer excludeId) {
        if (phone == null || phone.isEmpty()) {
            return;
        }
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid).eq("phone", phone);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        if (chatServiceMapper.selectCount(wrapper) > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Customer service representative with this phone number already exists");
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private int parseFlag(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0 ? 1 : 0;
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(str)) {
            return 1;
        }
        if ("false".equalsIgnoreCase(str)) {
            return 0;
        }
        try {
            return Integer.parseInt(str) != 0 ? 1 : 0;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private int parseIntOrDefault(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Integer ensureChatUserAssociation(String appid, String phone, String nickname, String avatar) {
        if (appid == null || appid.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
            return null;
        }

        QueryWrapper<ChatUserEntity> existingWrapper = new QueryWrapper<>();
        existingWrapper.eq("appid", appid).eq("phone", phone);
        ChatUserEntity user = chatUserMapper.selectOne(existingWrapper);
        LocalDateTime now = LocalDateTime.now();

        if (user != null) {
            boolean changed = false;
            if (nickname != null && !nickname.isBlank() && !Objects.equals(user.getNickname(), nickname)) {
                user.setNickname(nickname);
                changed = true;
            }
            if (avatar != null && !avatar.isBlank() && !Objects.equals(user.getAvatar(), avatar)) {
                user.setAvatar(avatar);
                changed = true;
            }
            if (!Objects.equals(user.getIsKefu(), 1)) {
                user.setIsKefu(1);
                changed = true;
            }
            if (!Objects.equals(user.getIsDelete(), 0)) {
                user.setIsDelete(0);
                changed = true;
            }
            if (changed) {
                user.setUpdateTime(now);
                chatUserMapper.updateById(user);
            }
            return user.getId();
        }

        QueryWrapper<ChatUserEntity> maxUidWrapper = new QueryWrapper<>();
        maxUidWrapper.eq("appid", appid).orderByDesc("uid").select("uid").last("limit 1");
        ChatUserEntity maxUidUser = chatUserMapper.selectOne(maxUidWrapper);
        int nextUid = (maxUidUser != null && maxUidUser.getUid() != null) ? maxUidUser.getUid() + 1 : 1;

        ChatUserEntity newUser = new ChatUserEntity();
        newUser.setAppid(appid);
        newUser.setPhone(phone);
        newUser.setNickname(nickname);
        newUser.setAvatar(avatar);
        newUser.setUid(nextUid);
        newUser.setIsTourist(0);
        newUser.setIsDelete(0);
        newUser.setIsKefu(1);
        newUser.setType(0);
        newUser.setGroupId(0);
        newUser.setCreateTime(now);
        newUser.setUpdateTime(now);

        chatUserMapper.insert(newUser);
        return newUser.getId();
    }

    /**
     * 获取分组选项列表
     * PHP: $seervice->getOptions()
     */
    private List<io.renren.crmchat.formbuilder.components.OptionComponent> getGroupOptions(String appid) {
        QueryWrapper<ChatServiceGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.orderByAsc("sort");
        List<ChatServiceGroupEntity> groups = chatServiceGroupMapper.selectList(wrapper);

        List<io.renren.crmchat.formbuilder.components.OptionComponent> options = new java.util.ArrayList<>();
        options.add(new io.renren.crmchat.formbuilder.components.OptionComponent(0, "请选择分组", false));

        for (ChatServiceGroupEntity group : groups) {
            options.add(new io.renren.crmchat.formbuilder.components.OptionComponent(
                group.getId(),
                group.getName(),
                false
            ));
        }

        return options;
    }

    private void syncChatUserAfterUpdate(ChatServiceEntity kefu) {
        String appid = kefu.getAppid();
        if (appid == null || appid.trim().isEmpty()) {
            return;
        }

        String phone = stringValue(kefu.getPhone());
        String nickname = stringValue(kefu.getNickname());
        String avatar = stringValue(kefu.getAvatar());

        if (kefu.getUserId() == null) {
            Integer userId = ensureChatUserAssociation(appid, phone, nickname, avatar);
            if (userId != null) {
                kefu.setUserId(userId);
                kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
                chatServiceMapper.updateById(kefu);
            }
            return;
        }

        ChatUserEntity user = chatUserMapper.selectById(kefu.getUserId());
        if (user == null) {
            Integer userId = ensureChatUserAssociation(appid, phone, nickname, avatar);
            if (userId != null) {
                kefu.setUserId(userId);
                kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
                chatServiceMapper.updateById(kefu);
            }
            return;
        }

        boolean changed = false;
        if (!phone.isEmpty() && !phone.equals(user.getPhone())) {
            user.setPhone(phone);
            changed = true;
        }
        if (!nickname.isEmpty() && !Objects.equals(user.getNickname(), nickname)) {
            user.setNickname(nickname);
            changed = true;
        }
        if (!avatar.isEmpty() && !Objects.equals(user.getAvatar(), avatar)) {
            user.setAvatar(avatar);
            changed = true;
        }
        if (!Objects.equals(user.getIsKefu(), 1)) {
            user.setIsKefu(1);
            changed = true;
        }
        if (!Objects.equals(user.getIsDelete(), 0)) {
            user.setIsDelete(0);
            changed = true;
        }
        if (changed) {
            user.setUpdateTime(LocalDateTime.now());
            chatUserMapper.updateById(user);
        }
    }
}
