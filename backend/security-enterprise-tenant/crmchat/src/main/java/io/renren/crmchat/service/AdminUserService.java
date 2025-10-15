package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatUserGroupMapper;
import io.renren.crmchat.dao.ChatUserLabelAssistMapper;
import io.renren.crmchat.dao.ChatUserLabelCateMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.entity.ChatUserLabelAssistEntity;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.common.constant.TenantConstants;
import io.renren.crmchat.security.TenantContextUtils;
import io.renren.crmchat.security.TenantQueryHelper;
import io.renren.crmchat.security.TenantSecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Admin User Service - 管理员用户管理
 * PHP Reference: User.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminUserService {

    private final ChatUserMapper chatUserMapper;
    private final ChatUserLabelAssistMapper chatUserLabelAssistMapper;
    private final ChatUserGroupMapper chatUserGroupMapper;
    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final ChatUserLabelMapper chatUserLabelMapper;

    /**
     * 获取用户列表
     * PHP Reference: User.php::index()
     */
    public Map<String, Object> getChatUserList(Map<String, Object> where, String appid) {
        Optional<String> tenantAppid = resolveAppid(appid);

        QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
        tenantAppid.ifPresent(a -> wrapper.eq("appid", a));

        // nickname查询 - 参考PHP getUserModel lines 142-147
        String nickname = (String) where.get("nickname");
        String fieldKey = (String) where.get("field_key");
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (fieldKey != null && !fieldKey.trim().isEmpty() &&
                Arrays.asList("id", "phone", "nickname").contains(fieldKey)) {
                // 精确字段查询
                wrapper.eq(fieldKey, nickname);
            } else {
                // 多字段模糊查询: nickname|id|phone
                wrapper.and(w -> w.like("nickname", nickname)
                        .or().like("id", nickname)
                        .or().like("phone", nickname));
            }
        }

        // group_id精确查询
        Integer groupId = (Integer) where.get("group_id");
        if (groupId != null && groupId > 0) {
            wrapper.eq("group_id", groupId);
        }

        // label_id - 支持逗号分隔的多个标签ID (参考PHP getUserModel lines 150-156)
        String labelIdStr = (String) where.get("label_id");
        if (labelIdStr != null && !labelIdStr.trim().isEmpty()) {
            // 解析逗号分隔的标签ID
            List<Integer> labelIds = new ArrayList<>();
            for (String id : labelIdStr.split(",")) {
                try {
                    int labelId = Integer.parseInt(id.trim());
                    if (labelId > 0) {
                        labelIds.add(labelId);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效ID
                }
            }

            if (!labelIds.isEmpty()) {
                // 先查询有这些标签的用户ID列表
                QueryWrapper<ChatUserLabelAssistEntity> assistWrapper = new QueryWrapper<>();
                tenantAppid.ifPresent(a -> assistWrapper.eq("appid", a));
                assistWrapper.in("label_id", labelIds);
                List<ChatUserLabelAssistEntity> labelAssists = chatUserLabelAssistMapper.selectList(assistWrapper);
                List<Integer> userIds = new ArrayList<>();
                for (ChatUserLabelAssistEntity assist : labelAssists) {
                    if (!userIds.contains(assist.getUserId())) {
                        userIds.add(assist.getUserId());
                    }
                }
                if (userIds.isEmpty()) {
                    // 没有用户有这些标签
                    return createEmptyResult();
                }
                wrapper.in("id", userIds);
            }
        }

        // time时间范围查询 - PHP格式: "2024-01-01 - 2024-01-31"
        String time = (String) where.get("time");
        if (time != null && !time.trim().isEmpty()) {
            String[] times = time.split(" - ");
            if (times.length == 2) {
                wrapper.between("create_time", times[0], times[1]);
            }
        }

        // sex性别
        Integer sex = (Integer) where.get("sex");
        if (sex != null && sex >= 0) {
            wrapper.eq("sex", sex);
        }

        // user_type用户类型
        String userType = (String) where.get("user_type");
        if (userType != null && !userType.trim().isEmpty()) {
            wrapper.eq("user_type", userType);
        }

        // field_key自定义字段查询 (暂时忽略,需要扩展字段支持)

        // is_tourist是否游客 (参考PHP getUserModel lines 161-163)
        // PHP逻辑: is_tourist=2时查询所有,转换为0; 否则按值查询
        String isTouristStr = (String) where.get("is_tourist");
        if (isTouristStr != null && !isTouristStr.trim().isEmpty()) {
            int isTourist = Integer.parseInt(isTouristStr);
            if (isTourist != 2) {  // 2表示查询所有,不添加条件
                wrapper.eq("is_tourist", isTourist == 2 ? 0 : isTourist);
            }
        }

        // 分页
        Integer page = (Integer) where.getOrDefault("page", 1);
        Integer limit = (Integer) where.getOrDefault("limit", 20);
        Page<ChatUserEntity> pageObj = new Page<>(page, limit);

        IPage<ChatUserEntity> result = chatUserMapper.selectPage(pageObj, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("count", Math.toIntExact(result.getTotal()));
        return response;
    }

    /**
     * 获取用户标签搜索列表
     * PHP Reference: User.php::getLavelAll()
     */
    public List<Map<String, Object>> getUserLabelSearchList(String appid) {
        // Category是平台级设置，不需要appid隔离
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0).orderByAsc("sort");

        List<ChatUserLabelCateEntity> cateList = chatUserLabelCateMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatUserLabelCateEntity cate : cateList) {
            Map<String, Object> item = new HashMap<>();

            // 获取该分类下的标签（Label是租户级数据，需要appid隔离）
            QueryWrapper<ChatUserLabelEntity> labelWrapper = new QueryWrapper<>();
            TenantQueryHelper.applyAppid(labelWrapper, appid);
            labelWrapper.eq("cate_id", cate.getId())
                    .eq("user_id", 0)
                    .orderByAsc("sort");
            List<ChatUserLabelEntity> labels = chatUserLabelMapper.selectList(labelWrapper);

            // 转换标签格式
            List<Map<String, Object>> options = new ArrayList<>();
            for (ChatUserLabelEntity label : labels) {
                Map<String, Object> option = new HashMap<>();
                option.put("value", label.getId());
                option.put("label", label.getLabel());
                options.add(option);
            }

            item.put("value", cate.getId());
            item.put("label", cate.getName());
            item.put("options", options);
            result.add(item);
        }

        return result;
    }

    /**
     * 获取修改用户表单
     * PHP Reference: User.php::edit()
     */
    public Map<String, Object> getChatUserForm(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        Optional<String> tenantAppid = resolveAppid(appid);

        ChatUserEntity user = chatUserMapper.selectById(id);
        if (user == null || (tenantAppid.isPresent() && !tenantAppid.get().equals(user.getAppid()))) {
            throw new CrmChatException("User does not exist");
        }

        QueryWrapper<ChatUserLabelAssistEntity> assistWrapper = new QueryWrapper<>();
        tenantAppid.ifPresent(a -> assistWrapper.eq("appid", a));
        assistWrapper.eq("user_id", id);

        List<ChatUserLabelAssistEntity> labelAssists = chatUserLabelAssistMapper.selectList(assistWrapper);

        // 获取用户的标签ID列表
        List<Integer> labelIds = new ArrayList<>();
        for (ChatUserLabelAssistEntity assist : labelAssists) {
            labelIds.add(assist.getLabelId());
        }

        // 获取用户分组列表作为下拉选项
        QueryWrapper<ChatUserGroupEntity> groupWrapper = new QueryWrapper<>();
        tenantAppid.ifPresent(a -> groupWrapper.eq("appid", a));
        groupWrapper.orderByAsc("id");
        List<ChatUserGroupEntity> groups = chatUserGroupMapper.selectList(groupWrapper);

        List<Map<String, Object>> groupOptions = new ArrayList<>();
        for (ChatUserGroupEntity group : groups) {
            Map<String, Object> option = new HashMap<>();
            option.put("value", group.getId());
            option.put("label", group.getGroupName());
            groupOptions.add(option);
        }

        // 构建form-create兼容的表单规则（参考PHP: Form::frameImage, Form::input等）
        List<Map<String, Object>> rules = new ArrayList<>();

        // 头像字段
        Map<String, Object> avatarRule = new HashMap<>();
        avatarRule.put("type", "frame");
        avatarRule.put("field", "avatar");
        avatarRule.put("title", "用户头像");
        avatarRule.put("value", user.getAvatar() != null ? user.getAvatar() : "");
        Map<String, Object> avatarProps = new HashMap<>();
        avatarProps.put("type", "image");
        avatarProps.put("src", "/admin/widget.images/index");
        avatarProps.put("icon", "ios-image");
        avatarProps.put("width", "950px");
        avatarProps.put("height", "420px");
        avatarRule.put("props", avatarProps);
        rules.add(avatarRule);

        // 昵称字段
        Map<String, Object> nicknameRule = new HashMap<>();
        nicknameRule.put("type", "input");
        nicknameRule.put("field", "nickname");
        nicknameRule.put("title", "用户昵称");
        nicknameRule.put("value", user.getNickname() != null ? user.getNickname() : "");
        rules.add(nicknameRule);

        // 备注昵称字段
        Map<String, Object> remarkNicknameRule = new HashMap<>();
        remarkNicknameRule.put("type", "input");
        remarkNicknameRule.put("field", "remark_nickname");
        remarkNicknameRule.put("title", "备注昵称");
        remarkNicknameRule.put("value", user.getRemarkNickname() != null ? user.getRemarkNickname() : "");
        rules.add(remarkNicknameRule);

        // 手机号字段
        Map<String, Object> phoneRule = new HashMap<>();
        phoneRule.put("type", "input");
        phoneRule.put("field", "phone");
        phoneRule.put("title", "手机号");
        phoneRule.put("value", user.getPhone() != null ? user.getPhone() : "");
        rules.add(phoneRule);

        // 用户分组下拉框（参考PHP OptionsRule.php: options直接在根级别，不在props内）
        Map<String, Object> groupRule = new HashMap<>();
        groupRule.put("type", "select");
        groupRule.put("field", "group_id");
        groupRule.put("title", "用户分组");
        groupRule.put("value", user.getGroupId() != null ? user.getGroupId() : 0);
        groupRule.put("options", groupOptions);  // ✅ 修复：options直接在根级别，参考PHP form-builder
        rules.add(groupRule);

        // 用户备注文本域
        Map<String, Object> remarksRule = new HashMap<>();
        remarksRule.put("type", "textarea");
        remarksRule.put("field", "remarks");
        remarksRule.put("title", "用户备注");
        remarksRule.put("value", user.getRemarks() != null ? user.getRemarks() : "");
        rules.add(remarksRule);

        // 返回form-create格式（参考PHP: create_form函数返回值）
        Map<String, Object> result = new HashMap<>();
        result.put("rules", rules);
        result.put("title", "修改用户");
        result.put("action", "user/" + id);  // ✅ 修复：去掉/api/admin前缀，避免与前端baseURL重复拼接
        result.put("method", "PUT");
        result.put("info", "");
        result.put("status", true);

        return result;
    }

    /**
     * 修改用户
     * PHP Reference: User.php::update()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateChatUser(Integer id, Map<String, Object> data, String appid) {
        String avatar = (String) data.get("avatar");
        String nickname = (String) data.get("nickname");
        Integer groupId = (Integer) data.getOrDefault("group_id", 0);
        String remarks = (String) data.get("remarks");
        String remarkNickname = (String) data.get("remark_nickname");
        String phone = (String) data.get("phone");

        if (avatar == null || avatar.trim().isEmpty()) {
            throw new CrmChatException("User avatar is required");
        }
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CrmChatException("User nickname is required");
        }

        ChatUserEntity user = chatUserMapper.selectById(id);
        Optional<String> tenantAppid = resolveAppid(appid);
        if (user == null || (tenantAppid.isPresent() && !tenantAppid.get().equals(user.getAppid()))) {
            throw new CrmChatException("User does not exist");
        }

        // 更新用户信息
        user.setAvatar(avatar);
        user.setNickname(nickname);
        user.setGroupId(groupId);
        user.setRemarks(remarks);
        user.setRemarkNickname(remarkNickname);
        user.setPhone(phone);

        boolean success = chatUserMapper.updateById(user) > 0;

        // TODO: 如果头像或昵称有变化,同步更新ChatServiceRecord
        // PHP logic: if avatar/nickname changed, update chat_service_record where to_user_id = id
        // This requires ChatServiceRecordMapper implementation

        return success;
    }

    /**
     * 批量修改用户标签
     * PHP Reference: User.php::batchLabel()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateLabel(List<Integer> userIds, List<Integer> labelIds, List<Integer> unLabelIds, String appid) {
        if (userIds == null || userIds.isEmpty()) {
            throw new CrmChatException("Please select at least one user");
        }
        if ((labelIds == null || labelIds.isEmpty()) && (unLabelIds == null || unLabelIds.isEmpty())) {
            throw new CrmChatException("Please set or unset at least one tag");
        }

        // 添加标签
        Optional<String> tenantAppid = resolveAppid(appid);

        if (labelIds != null && !labelIds.isEmpty()) {
            for (Integer userId : userIds) {
                ChatUserEntity user = chatUserMapper.selectById(userId);
                if (user == null) {
                    continue;
                }
                if (tenantAppid.isPresent() && !tenantAppid.get().equals(user.getAppid())) {
                    throw new CrmChatException("User does not exist");
                }
                String userAppid = user.getAppid();
                for (Integer labelId : labelIds) {
                    // 检查是否已存在
                    long count = chatUserLabelAssistMapper.selectCount(
                            new QueryWrapper<ChatUserLabelAssistEntity>()
                                    .eq("appid", userAppid)
                                    .eq("user_id", userId)
                                    .eq("label_id", labelId)
                    );
                    if (count == 0) {
                        ChatUserLabelAssistEntity assist = new ChatUserLabelAssistEntity();
                        assist.setAppid(userAppid);
                        assist.setUserId(userId);
                        assist.setLabelId(labelId);
                        chatUserLabelAssistMapper.insert(assist);
                    }
                }
            }
        }

        // 删除标签
        if (unLabelIds != null && !unLabelIds.isEmpty()) {
            for (Integer userId : userIds) {
                ChatUserEntity user = chatUserMapper.selectById(userId);
                if (user == null) {
                    continue;
                }
                if (tenantAppid.isPresent() && !tenantAppid.get().equals(user.getAppid())) {
                    throw new CrmChatException("User does not exist");
                }
                String userAppid = user.getAppid();
                for (Integer labelId : unLabelIds) {
                    chatUserLabelAssistMapper.delete(
                            new QueryWrapper<ChatUserLabelAssistEntity>()
                                    .eq("appid", userAppid)
                                    .eq("user_id", userId)
                                    .eq("label_id", labelId)
                    );
                }
            }
        }

        return true;
    }

    /**
     * 批量修改用户分组
     * PHP Reference: User.php::batchGroup()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateGroup(List<Integer> userIds, Integer groupId, String appid) {
        if (userIds == null || userIds.isEmpty()) {
            throw new CrmChatException("Please select at least one user");
        }
        if (groupId == null || groupId <= 0) {
            throw new CrmChatException("Please select a group");
        }

        Optional<String> tenantAppid = resolveAppid(appid);

        // 批量更新
        for (Integer userId : userIds) {
            ChatUserEntity user = chatUserMapper.selectById(userId);
            if (user == null) {
                continue;
            }
            if (tenantAppid.isPresent() && !tenantAppid.get().equals(user.getAppid())) {
                throw new CrmChatException("User does not exist");
            }
            if (user != null) {
                user.setGroupId(groupId);
                chatUserMapper.updateById(user);
            }
        }

        return true;
    }

    /**
     * 获取全部标签
     * PHP Reference: User.php::getLabelAll()
     */
    public List<Map<String, Object>> getLabelAll(Integer userId, String appid) {
        // Category是平台级设置，不需要appid隔离
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0).orderByAsc("sort");

        List<ChatUserLabelCateEntity> cateList = chatUserLabelCateMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatUserLabelCateEntity cate : cateList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cate.getId());
            item.put("name", cate.getName());

            // 获取该分类下的标签（Label是租户级数据，需要appid隔离）
            QueryWrapper<ChatUserLabelEntity> labelWrapper = new QueryWrapper<>();
            TenantQueryHelper.applyAppid(labelWrapper, appid);
            labelWrapper.eq("cate_id", cate.getId())
                    .eq("user_id", 0)
                    .orderByAsc("sort");
            List<ChatUserLabelEntity> labels = chatUserLabelMapper.selectList(labelWrapper);
            item.put("label", labels);

            result.add(item);
        }

        return result;
    }

    /**
     * 获取全部分组
     * PHP Reference: User.php::getGroupAll()
     */
    public List<ChatUserGroupEntity> getGroupAll(String appid) {
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, appid);
        wrapper.orderByAsc("id");
        return chatUserGroupMapper.selectList(wrapper);
    }

    /**
     * 创建空结果
     */
    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("count", 0);
        return result;
    }

    private Optional<String> resolveAppid(String appid) {
        if (TenantContextUtils.isTenantIsolationEnabled()) {
            return Optional.of(TenantSecurityUtils.requireAppid());
        }
        if (TenantContextUtils.isSuperAdmin()) {
            if (appid != null && !appid.trim().isEmpty() && !TenantConstants.SUPER_APPID.equals(appid.trim())) {
                return Optional.of(appid.trim());
            }
            return Optional.empty();
        }
        if (appid != null && !appid.trim().isEmpty()) {
            return Optional.of(appid.trim());
        }
        return Optional.empty();
    }
}
