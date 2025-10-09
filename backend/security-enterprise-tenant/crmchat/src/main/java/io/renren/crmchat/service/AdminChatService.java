package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.page.PageData;
import io.renren.crmchat.dao.ApplicationMapper;
import io.renren.crmchat.dao.ChatServiceDialogueRecordMapper;
import io.renren.crmchat.dao.ChatServiceGroupMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ApplicationEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceGroupEntity;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.service.common.PaginationService;
import io.renren.crmchat.service.common.PasswordService;
import io.renren.crmchat.service.common.TokenService;
import io.renren.crmchat.service.common.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Admin 客服管理服务
 * PHP Reference: /app/services/chat/ChatServices.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminChatService {

    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,30}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[0-9a-z_$]{6,20}$", Pattern.CASE_INSENSITIVE);

    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceGroupMapper chatServiceGroupMapper;
    private final ChatUserMapper chatUserMapper;
    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ApplicationMapper applicationMapper;
    private final PaginationService paginationService;
    private final ValidationService validationService;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    /**
     * 获取站点统计
     * PHP Reference: ChatServices::getChatStatistics()
     *
     * @return 统计数据
     */
    public Map<String, Object> getChatStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 总客服数
        Long totalServices = chatServiceMapper.selectCount(new QueryWrapper<>());
        stats.put("total_services", totalServices);

        // 2. 在线客服数
        QueryWrapper<ChatServiceEntity> onlineWrapper = new QueryWrapper<>();
        onlineWrapper.eq("online", 1).eq("status", 1);
        Long onlineServices = chatServiceMapper.selectCount(onlineWrapper);
        stats.put("online_services", onlineServices);

        // 3. 启用的客服数
        QueryWrapper<ChatServiceEntity> enabledWrapper = new QueryWrapper<>();
        enabledWrapper.eq("status", 1);
        Long enabledServices = chatServiceMapper.selectCount(enabledWrapper);
        stats.put("enabled_services", enabledServices);

        // 4. 禁用的客服数
        QueryWrapper<ChatServiceEntity> disabledWrapper = new QueryWrapper<>();
        disabledWrapper.eq("status", 0);
        Long disabledServices = chatServiceMapper.selectCount(disabledWrapper);
        stats.put("disabled_services", disabledServices);

        // TODO: 添加聊天次数和用户数统计（需要 eb_chat_record 和 eb_chat_user 表支持）
        // stats.put("total_chats", 0);
        // stats.put("today_chats", 0);
        // stats.put("total_users", 0);
        // stats.put("today_users", 0);

        return stats;
    }

    /**
     * 获取客服列表（带分页和搜索）
     * PHP Reference: ChatServices::getKefuList()
     *
     * @param params 查询参数
     * @return 分页数据
     */
    public PageData<Map<String, Object>> getKefuList(Map<String, Object> params) {
        // 1. 提取分页参数
        PaginationService.PaginationParams pagination = paginationService.extractParams(params);

        // 2. 构建查询条件
        QueryWrapper<ChatServiceEntity> wrapper = buildKefuQueryWrapper(params);

        // 3. 分页查询
        List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper
                .last(String.format("LIMIT %d OFFSET %d", pagination.getLimit(), pagination.getOffset()))
        );

        // 4. 查询总数
        QueryWrapper<ChatServiceEntity> countWrapper = buildKefuQueryWrapper(params);
        Long total = chatServiceMapper.selectCount(countWrapper);

        // 5. 处理数据（添加分组名称）
        List<Map<String, Object>> list = new ArrayList<>();
        for (ChatServiceEntity kefu : kefuList) {
            Map<String, Object> item = entityToMap(kefu);

            // 添加分组名称
            if (kefu.getGroupId() != null && kefu.getGroupId() > 0) {
                ChatServiceGroupEntity group = chatServiceGroupMapper.selectById(kefu.getGroupId());
                item.put("group_name", group != null ? group.getName() : "");
            } else {
                item.put("group_name", "");
            }

            list.add(item);
        }

        // 6. 返回分页数据
        return paginationService.createPageData(list, total);
    }

    /**
     * 获取客服分组列表
     * PHP Reference: ChatServices::getKefuGroupList()
     *
     * @return 分组列表
     */
    public List<Map<String, Object>> getKefuGroupList() {
        // 查询所有分组，按排序字段倒序
        QueryWrapper<ChatServiceGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort");
        List<ChatServiceGroupEntity> groups = chatServiceGroupMapper.selectList(wrapper);

        // 统计每个分组的客服数量
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatServiceGroupEntity group : groups) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", group.getId());
            item.put("name", group.getName());
            item.put("sort", group.getSort());
            item.put("appid", group.getAppid());

            // 统计该分组下的客服数量
            QueryWrapper<ChatServiceEntity> kefuWrapper = new QueryWrapper<>();
            kefuWrapper.eq("group_id", group.getId());
            Long kefuCount = chatServiceMapper.selectCount(kefuWrapper);
            item.put("kefu_count", kefuCount);

            result.add(item);
        }

        return result;
    }

    /**
     * 创建客服
     * PHP Reference: ChatServices::createKefu()
     *
     * @param data 客服数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void createKefu(Map<String, Object> data) {
        validateCreateKefuParams(data);

        String account = data.get("account").toString();
        String password = data.get("password").toString();
        String truePassword = stringValue(data.get("true_password"));
        if (!truePassword.isEmpty() && !password.equals(truePassword)) {
            throw new CrmChatException("Passwords do not match");
        }

        if (checkAccountExists(account)) {
            throw new CrmChatException("This customer service account already exists");
        }

        String phone = data.get("phone").toString();
        ensurePhoneUniqueGlobal(phone, null);

        String appid = resolveDefaultAppid();
        if (appid == null || appid.trim().isEmpty()) {
            throw new CrmChatException("No available application configured, please create an application first");
        }

        ChatServiceEntity kefu = new ChatServiceEntity();
        kefu.setAppid(appid);
        kefu.setAccount(account);
        kefu.setPassword(passwordService.hash(password));
        kefu.setNickname(data.get("nickname").toString());
        kefu.setAvatar(data.get("avatar").toString());
        kefu.setPhone(phone);
        kefu.setGroupId(parseIntOrDefault(data.get("group_id"), 0));
        kefu.setStatus(parseFlag(data.get("status"), 1));
        kefu.setWelcomeWords((String) data.getOrDefault("welcome_words", "Hello, how may I assist you?"));
        kefu.setAutoReply(parseFlag(data.get("auto_reply"), 0));
        kefu.setNotify(parseFlag(data.get("notify"), 1));
        kefu.setCustomer(parseFlag(data.get("customer"), 1));
        kefu.setOnline(0);
        kefu.setIsApp(0);
        kefu.setIsBackstage(0);
        kefu.setUniqid(UUID.randomUUID().toString());

        int currentTime = (int) (System.currentTimeMillis() / 1000);
        kefu.setAddTime(currentTime);
        kefu.setUpdateTime(currentTime);

        int result = chatServiceMapper.insert(kefu);
        if (result <= 0 || kefu.getId() == null) {
            throw new CrmChatException("Failed to add customer service representative, please try again later");
        }

        Integer userId = ensureChatUserAssociation(appid, phone, kefu.getNickname(), kefu.getAvatar());
        if (userId != null) {
            kefu.setUserId(userId);
            kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));
            chatServiceMapper.updateById(kefu);
        }
    }

    /**
     * 更新客服
     * PHP Reference: ChatServices::updateKefu()
     *
     * @param id   客服ID
     * @param data 更新数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateKefu(Integer id, Map<String, Object> data) {
        // 1. 检查客服是否存在
        ChatServiceEntity kefu = chatServiceMapper.selectById(id);
        if (kefu == null) {
            throw new CrmChatException("Data does not exist");
        }

        if (data.containsKey("nickname")) {
            String nickname = stringValue(data.get("nickname"));
            if (nickname.isEmpty()) {
                throw new CrmChatException("Customer service name cannot be empty");
            }
            kefu.setNickname(nickname);
        }

        if (data.containsKey("account") && data.get("account") != null) {
            String account = stringValue(data.get("account"));
            if (!account.isEmpty() && !account.equals(kefu.getAccount())) {
                if (!ACCOUNT_PATTERN.matcher(account).matches()) {
                    throw new CrmChatException("Account must be 4-30 alphanumeric characters");
                }
                QueryWrapper<ChatServiceEntity> accountWrapper = new QueryWrapper<>();
                accountWrapper.eq("account", account).ne("id", id);
                if (chatServiceMapper.selectCount(accountWrapper) > 0) {
                    throw new CrmChatException("This customer service account already exists");
                }
                kefu.setAccount(account);
            }
        }

        if (data.containsKey("avatar")) {
            String avatar = stringValue(data.get("avatar"));
            if (avatar.isEmpty()) {
                throw new CrmChatException("Please select customer service avatar");
            }
            kefu.setAvatar(avatar);
        }

        if (data.containsKey("phone") && data.get("phone") != null) {
            String phone = stringValue(data.get("phone"));
            if (!phone.isEmpty()) {
                validationService.validatePhone(phone);
                ensurePhoneUniqueGlobal(phone, id);
                kefu.setPhone(phone);
            }
        }

        if (data.containsKey("group_id") && data.get("group_id") != null) {
            int groupId = parseIntOrDefault(data.get("group_id"), kefu.getGroupId());
            kefu.setGroupId(groupId);
        }

        if (data.containsKey("status") && data.get("status") != null) {
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

        if (data.containsKey("password") && data.get("password") != null) {
            String password = stringValue(data.get("password"));
            if (!password.isEmpty()) {
                if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    throw new CrmChatException("Password must be 6-20 alphanumeric characters");
                }
                String truePassword = stringValue(data.get("true_password"));
                if (!truePassword.isEmpty() && !password.equals(truePassword)) {
                    throw new CrmChatException("Passwords do not match");
                }
                kefu.setPassword(passwordService.hash(password));
            }
        }

        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new CrmChatException("Update failed, please try again later");
        }

        syncChatUserAfterUpdate(kefu);
    }

    /**
     * 删除客服
     * PHP Reference: ChatServices::deleteKefu()
     *
     * @param id 客服ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKefu(Integer id) {
        // 1. 检查客服是否存在
        ChatServiceEntity kefu = chatServiceMapper.selectById(id);
        if (kefu == null) {
            throw new CrmChatException("Data does not exist");
        }

        // 2. 物理删除客服
        int result = chatServiceMapper.deleteById(id);
        if (result <= 0) {
            throw new CrmChatException("Delete failed, please try again later");
        }

        // TODO: 可选 - 清除相关聊天记录、客户关系等（根据业务需求）
    }

    /**
     * 更新客服状态
     * PHP Reference: ChatServices::updateKefuStatus()
     *
     * @param id     客服ID
     * @param status 状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateKefuStatus(Integer id, Integer status) {
        // 1. 检查客服是否存在
        ChatServiceEntity kefu = chatServiceMapper.selectById(id);
        if (kefu == null) {
            throw new CrmChatException("Data does not exist");
        }

        // 2. 更新状态
        kefu.setStatus(status);
        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        // 3. 保存
        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new CrmChatException("Status update failed");
        }
    }

    /**
     * 获取聊天记录（带分页）
     * PHP Reference: ChatServices::getChatRecords()
     *
     * @param params 查询参数
     * @return 分页数据
     */
    public PageData<Map<String, Object>> getChatRecords(Map<String, Object> params) {
        // TODO: 实现聊天记录查询（需要 eb_chat_record 表支持）
        // 当前返回空数据
        PaginationService.PaginationParams pagination = paginationService.extractParams(params);
        List<Map<String, Object>> emptyList = new ArrayList<>();
        return paginationService.createPageData(emptyList, 0L);
    }

    /**
     * 获取客服关联的聊天用户列表
     * PHP Reference: Service.php::chat_user()
     *
     * @param kefuId 客服ID
     * @return 聊天过的用户列表
     */
    public List<Map<String, Object>> getChatUserList(Integer kefuId) {
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || kefu.getUserId() == null || kefu.getUserId() <= 0) {
            throw new CrmChatException("Data does not exist");
        }

        Integer chatUserId = kefu.getUserId();
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        String appid = stringValue(kefu.getAppid());
        if (!appid.isEmpty()) {
            wrapper.eq("appid", appid);
        }
        wrapper.and(w -> w.eq("user_id", chatUserId).or().eq("to_user_id", chatUserId));
        wrapper.select("user_id", "to_user_id");

        List<ChatServiceDialogueRecordEntity> records = chatServiceDialogueRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> relatedUserIds = new LinkedHashSet<>();
        for (ChatServiceDialogueRecordEntity record : records) {
            if (record.getUserId() != null) {
                relatedUserIds.add(record.getUserId());
            }
            if (record.getToUserId() != null) {
                relatedUserIds.add(record.getToUserId());
            }
        }
        relatedUserIds.remove(chatUserId);
        relatedUserIds.remove(null);

        if (relatedUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        QueryWrapper<ChatUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.in("id", relatedUserIds);
        List<ChatUserEntity> users = chatUserMapper.selectList(userWrapper);

        Map<Integer, ChatUserEntity> userIndex = new HashMap<>();
        for (ChatUserEntity user : users) {
            if (user != null && user.getId() != null) {
                userIndex.put(user.getId(), user);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Integer targetId : relatedUserIds) {
            ChatUserEntity user = userIndex.get(targetId);
            if (user == null) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", user.getId());
            item.put("uid", user.getUid());
            item.put("nickname", user.getNickname());
            item.put("headimgurl", user.getAvatar());
            item.put("avatar", user.getAvatar());
            result.add(item);
        }
        return result;
    }

    /**
     * 客服代登录（管理员）
     * PHP Reference: Service.php::keufLogin()
     *
     * @param id 客服ID
     * @return 登录结果（token + exp_time + kefuInfo）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> kefuLogin(Integer id) {
        ChatServiceEntity serviceInfo = chatServiceMapper.selectById(id);
        if (serviceInfo == null) {
            throw new CrmChatException("Logged-in customer service representative does not exist");
        }

        String account = stringValue(serviceInfo.getAccount());
        String password = stringValue(serviceInfo.getPassword());
        if (account.isEmpty() || password.isEmpty()) {
            throw new CrmChatException("Please enter customer service account and password before accessing the platform");
        }
        if (serviceInfo.getStatus() == null || serviceInfo.getStatus() != 1) {
            throw new CrmChatException("Customer service account has been disabled");
        }

        String appid = stringValue(serviceInfo.getAppid());
        if (appid.isEmpty()) {
            appid = resolveDefaultAppid();
        }

        String token = tokenService.generateKefuToken(serviceInfo.getId().longValue(), account, appid);
        Long expTime = tokenService.getTokenExpireAt(token);

        ChatServiceEntity updateEntity = new ChatServiceEntity();
        updateEntity.setId(serviceInfo.getId());
        updateEntity.setOnline(1);
        updateEntity.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        chatServiceMapper.updateById(updateEntity);

        Map<String, Object> kefuInfo = new HashMap<>();
        kefuInfo.put("id", serviceInfo.getId());
        kefuInfo.put("account", account);
        kefuInfo.put("nickname", serviceInfo.getNickname());
        kefuInfo.put("avatar", serviceInfo.getAvatar());
        kefuInfo.put("phone", serviceInfo.getPhone());
        kefuInfo.put("appid", appid);
        kefuInfo.put("group_id", serviceInfo.getGroupId());
        kefuInfo.put("online", 1);
        kefuInfo.put("is_app", serviceInfo.getIsApp());
        kefuInfo.put("is_backstage", serviceInfo.getIsBackstage());
        kefuInfo.put("auto_reply", serviceInfo.getAutoReply());
        kefuInfo.put("welcome_words", serviceInfo.getWelcomeWords());
        kefuInfo.put("uniqid", serviceInfo.getUniqid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        if (expTime != null) {
            result.put("exp_time", expTime);
        }
        result.put("kefuInfo", kefuInfo);
        return result;
    }

    /**
     * 构建客服查询条件
     */
    private QueryWrapper<ChatServiceEntity> buildKefuQueryWrapper(Map<String, Object> params) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();

        // 关键词搜索（账号、昵称、电话）
        if (params.containsKey("keyword") && params.get("keyword") != null) {
            String keyword = params.get("keyword").toString().trim();
            if (!keyword.isEmpty()) {
                wrapper.and(w -> w
                        .like("account", keyword)
                        .or().like("nickname", keyword)
                        .or().like("phone", keyword)
                );
            }
        }

        // 分组筛选
        if (params.containsKey("group_id") && params.get("group_id") != null) {
            String groupIdStr = params.get("group_id").toString().trim();
            if (!groupIdStr.isEmpty()) {
                try {
                    int groupId = Integer.parseInt(groupIdStr);
                    wrapper.eq("group_id", groupId);
                } catch (NumberFormatException ignored) {
                }
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

        // 默认按添加时间倒序
        wrapper.orderByDesc("add_time");

        return wrapper;
    }

    /**
     * 验证创建客服的参数
     */
    private void validateCreateKefuParams(Map<String, Object> data) {
        String avatar = stringValue(data.get("avatar"));
        if (avatar.isEmpty()) {
            throw new CrmChatException("Please select customer service avatar");
        }
        data.put("avatar", avatar);

        String phone = stringValue(data.get("phone"));
        if (phone.isEmpty()) {
            throw new CrmChatException("Please enter a valid phone number");
        }
        validationService.validatePhone(phone);
        data.put("phone", phone);

        String account = stringValue(data.get("account"));
        if (account.isEmpty()) {
            throw new CrmChatException("Please enter account");
        }
        if (!ACCOUNT_PATTERN.matcher(account).matches()) {
            throw new CrmChatException("Account must be 4-30 alphanumeric characters");
        }
        data.put("account", account);

        String password = stringValue(data.get("password"));
        if (password.isEmpty()) {
            throw new CrmChatException("Please enter password");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new CrmChatException("Password must be 6-20 alphanumeric characters");
        }
        data.put("password", password);

        String nickname = stringValue(data.get("nickname"));
        if (nickname.isEmpty()) {
            throw new CrmChatException("Please enter customer service nickname");
        }
        data.put("nickname", nickname);
    }

    /**
     * 检查账号是否存在
     */
    private boolean checkAccountExists(String account) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        return chatServiceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 实体转 Map
     */
    private Map<String, Object> entityToMap(ChatServiceEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("appid", entity.getAppid());
        map.put("group_id", entity.getGroupId());
        map.put("account", entity.getAccount());
        map.put("nickname", entity.getNickname());
        map.put("phone", entity.getPhone());
        map.put("avatar", entity.getAvatar());
        map.put("online", entity.getOnline());
        map.put("status", entity.getStatus());
        map.put("welcome_words", entity.getWelcomeWords());
        map.put("auto_reply", entity.getAutoReply());
        map.put("add_time", entity.getAddTime());
        map.put("update_time", entity.getUpdateTime());
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
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
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

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String resolveDefaultAppid() {
        QueryWrapper<ApplicationEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).orderByAsc("id").last("limit 1");
        ApplicationEntity application = applicationMapper.selectOne(wrapper);
        return application != null ? stringValue(application.getAppid()) : "";
    }

    private void ensurePhoneUniqueGlobal(String phone, Integer excludeId) {
        if (phone == null || phone.isEmpty()) {
            return;
        }
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        if (excludeId != null) {
            wrapper.ne("id", excludeId);
        }
        if (chatServiceMapper.selectCount(wrapper) > 0) {
            throw new CrmChatException("Customer service representative with this phone number already exists");
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
