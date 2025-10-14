package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceRecordMapper;
import io.renren.crmchat.dao.ChatUserGroupMapper;
import io.renren.crmchat.dao.ChatUserLabelAssistMapper;
import io.renren.crmchat.dao.ChatUserLabelCateMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.entity.ChatUserLabelAssistEntity;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tenant 用户管理服务
 * PHP Reference: /app/controller/tenant/user/User.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getChatUserList(): 获取用户列表
 *    - 支持多条件筛选（nickname, group_id, label_id, time, sex, user_type, field_key, is_tourist）
 *    - 分页查询
 * 2. getChatUserForm(): 获取用户编辑表单
 *    - 返回用户详情
 * 3. updateChatUser(): 更新用户信息
 *    - 验证avatar、nickname必填
 *    - 同步更新chat_service_record表的avatar和nickname
 * 4. batchUpdateGroup(): 批量设置分组
 *    - 批量更新用户的group_id
 * 5. batchUpdateLabel(): 批量设置标签
 *    - 支持添加标签（label_id）和移除标签（un_label_id）
 *    - 操作chat_user_label_assist表
 * 6. getLabelAllHierarchy(): 获取所有标签（层级结构）
 *    - 返回分类+标签的树形结构
 *    - 格式：label='分类名', value=分类id, options=[标签列表]
 * 7. getGroupAll(): 获取所有分组
 *    - 返回分组列表
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantUserService {

    private final ChatUserMapper chatUserMapper;
    private final ChatUserGroupMapper chatUserGroupMapper;
    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final ChatUserLabelMapper chatUserLabelMapper;
    private final ChatUserLabelAssistMapper chatUserLabelAssistMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;

    /**
     * 获取用户列表
     * GET /api/tenant/user
     *
     * PHP Reference: User.php::index()
     *
     * 业务逻辑:
     * 1. 多条件筛选（nickname, group_id, label_id, time, sex, user_type, field_key, is_tourist）
     * 2. appid隔离
     * 3. 分页查询
     *
     * @param filters      筛选条件
     * @return 分页用户列表
     */
    public Map<String, Object> getChatUserList(Map<String, Object> filters) {
        QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();

        String nicknameFilter = filters.get("nickname") != null ? filters.get("nickname").toString().trim() : null;
        String fieldKey = filters.get("field_key") != null ? filters.get("field_key").toString().trim() : null;

        if (nicknameFilter != null && !nicknameFilter.isEmpty()) {
            if (fieldKey != null && !fieldKey.isEmpty()) {
                switch (fieldKey) {
                    case "id":
                        try {
                            long idValue = Long.parseLong(nicknameFilter);
                            wrapper.eq("id", idValue);
                        } catch (NumberFormatException ex) {
                            wrapper.eq("id", -1);
                        }
                        break;
                    case "phone":
                        wrapper.eq("phone", nicknameFilter);
                        break;
                    case "nickname":
                        wrapper.eq("nickname", nicknameFilter);
                        break;
                    default:
                        wrapper.and(q -> q.like("nickname", nicknameFilter)
                                .or().like("id", nicknameFilter)
                                .or().like("phone", nicknameFilter));
                        break;
                }
            } else {
                wrapper.and(q -> q.like("nickname", nicknameFilter)
                        .or().like("id", nicknameFilter)
                        .or().like("phone", nicknameFilter));
            }
        }

        if (filters.containsKey("group_id") && filters.get("group_id") != null && !filters.get("group_id").toString().isEmpty()) {
            wrapper.eq("group_id", filters.get("group_id"));
        }

        if (filters.containsKey("sex") && filters.get("sex") != null && !filters.get("sex").toString().isEmpty()) {
            wrapper.eq("sex", filters.get("sex"));
        }

        if (filters.containsKey("user_type") && filters.get("user_type") != null && !filters.get("user_type").toString().isEmpty()) {
            wrapper.eq("type", filters.get("user_type"));
        }

        if (filters.containsKey("is_tourist") && filters.get("is_tourist") != null && !filters.get("is_tourist").toString().isEmpty()) {
            String isTourist = filters.get("is_tourist").toString();
            String finalValue = "2".equals(isTourist) ? "0" : isTourist;
            wrapper.eq("is_tourist", finalValue);
        }

        if (filters.containsKey("label_id") && filters.get("label_id") != null && !filters.get("label_id").toString().isEmpty()) {
            String labelIdsStr = filters.get("label_id").toString();
            List<Integer> labelIds = Arrays.stream(labelIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            if (!labelIds.isEmpty()) {
                String idList = labelIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                wrapper.inSql("id", "SELECT user_id FROM eb_chat_user_label_assist WHERE label_id IN (" + idList + ")");
            }
        }

        if (filters.containsKey("time") && filters.get("time") != null && !filters.get("time").toString().trim().isEmpty()) {
            String timeRange = filters.get("time").toString().trim();
            String[] parts = timeRange.split("-");
            if (parts.length >= 2) {
                String start = parts[0].trim();
                String end = parts[parts.length - 1].trim();
                if (start.equals(end)) {
                    end = end + " 23:59:59";
                }
                if (!start.contains(":")) {
                    start = start + " 00:00:00";
                }
                if (!end.contains(":")) {
                    end = end + " 23:59:59";
                }
                wrapper.ge("create_time", start);
                wrapper.le("create_time", end);
            }
        }

        wrapper.orderByDesc("id");

        int page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        int limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatUserEntity> pageObj = new Page<>(page, limit);
        Page<ChatUserEntity> pageResult = chatUserMapper.selectPage(pageObj, wrapper);

        List<ChatUserEntity> records = pageResult.getRecords();

        List<Map<String, Object>> list = new ArrayList<>();
        if (!records.isEmpty()) {
            List<Integer> userIds = records.stream()
                    .map(ChatUserEntity::getId)
                    .filter(Objects::nonNull)
                    .toList();

            Map<Integer, ChatUserGroupEntity> groupMap = new HashMap<>();
            List<Integer> groupIds = records.stream()
                    .map(ChatUserEntity::getGroupId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (!groupIds.isEmpty()) {
                chatUserGroupMapper.selectBatchIds(groupIds)
                        .forEach(group -> groupMap.put(group.getId(), group));
            }

            Map<Integer, List<Integer>> userLabelIdMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                QueryWrapper<ChatUserLabelAssistEntity> assistWrapper = new QueryWrapper<>();
                assistWrapper.in("user_id", userIds);
                List<ChatUserLabelAssistEntity> assists = chatUserLabelAssistMapper.selectList(assistWrapper);
                for (ChatUserLabelAssistEntity assist : assists) {
                    userLabelIdMap.computeIfAbsent(assist.getUserId(), k -> new ArrayList<>())
                            .add(assist.getLabelId());
                }
            }

            Map<Integer, ChatUserLabelEntity> labelMap = new HashMap<>();
            List<Integer> allLabelIds = userLabelIdMap.values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            if (!allLabelIds.isEmpty()) {
                chatUserLabelMapper.selectBatchIds(allLabelIds)
                        .forEach(label -> labelMap.put(label.getId(), label));
            }

            for (ChatUserEntity user : records) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("nickname", user.getNickname());
                item.put("avatar", user.getAvatar());
                item.put("phone", user.getPhone());
                item.put("group_id", user.getGroupId());
                item.put("appid", user.getAppid());
                item.put("type", user.getType());
                item.put("sex", user.getSex());
                item.put("is_tourist", user.getIsTourist());
                item.put("remarks", user.getRemarks());
                item.put("remark_nickname", user.getRemarkNickname());

                if (user.getGroupId() != null && groupMap.containsKey(user.getGroupId())) {
                    item.put("groupOne", groupMap.get(user.getGroupId()));
                }

                List<Integer> labelIds = userLabelIdMap.getOrDefault(user.getId(), List.of());
                if (!labelIds.isEmpty()) {
                    List<ChatUserLabelEntity> labels = labelIds.stream()
                            .map(labelMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    item.put("label", labels);
                } else {
                    item.put("label", List.of());
                }

                list.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        // 确保 count 是数字类型，不是字符串（Vue Page 组件要求 Number 类型）
        result.put("count", (int) pageResult.getTotal());
        return result;
    }

    /**
     * 获取用户编辑表单
     * GET /api/tenant/user/:id/edit
     *
     * PHP Reference: User.php::edit()
     *
     * 业务逻辑:
     * 1. 验证id非空
     * 2. 返回用户详情
     *
     * @param id           用户ID
     * @return 用户详情
     */
    public ChatUserEntity getChatUserForm(Integer id) {
        // PHP: if (!$id) return $this->fail('缺少参数');
        // PHP: return $this->success($this->services->getChatUserForm((int)$id));

        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatUserEntity user = chatUserMapper.selectById(id);
        if (user == null) {
            throw new io.renren.crmchat.exception.CrmChatException("User does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(user.getAppid(), "User does not exist");

        return user;
    }

    /**
     * 更新用户信息
     * PUT /api/tenant/user/:id
     *
     * PHP Reference: User.php::update()
     *
     * 业务逻辑:
     * 1. 验证avatar、nickname必填
     * 2. 更新用户信息
     * 3. 如果avatar或nickname变化，同步更新chat_service_record表
     *
     * @param id           用户ID
     * @param data         用户数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateChatUser(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['avatar']) return $this->fail('用户头像必须填写');
        if (!data.containsKey("avatar") || data.get("avatar") == null || data.get("avatar").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("User avatar is required");
        }

        // 2. PHP: if (!$data['nickname']) return $this->fail('用户昵称必须填写');
        if (!data.containsKey("nickname") || data.get("nickname") == null || data.get("nickname").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("User nickname is required");
        }

        // 3. 获取原用户信息
        ChatUserEntity userInfo = chatUserMapper.selectById(id);
        if (userInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("User does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(userInfo.getAppid(), "User does not exist");

        String oldAvatar = userInfo.getAvatar();
        String oldNickname = userInfo.getNickname();

        // 4. 更新用户信息
        ChatUserEntity user = new ChatUserEntity();
        user.setId(id);
        user.setAvatar(data.get("avatar").toString());
        user.setNickname(data.get("nickname").toString());

        if (data.containsKey("group_id") && data.get("group_id") != null) {
            user.setGroupId(Integer.parseInt(data.get("group_id").toString()));
        }
        if (data.containsKey("remarks") && data.get("remarks") != null) {
            user.setRemarks(data.get("remarks").toString());
        }
        if (data.containsKey("remark_nickname") && data.get("remark_nickname") != null) {
            user.setRemarkNickname(data.get("remark_nickname").toString());
        }
        if (data.containsKey("phone") && data.get("phone") != null) {
            user.setPhone(data.get("phone").toString());
        }

        int result = chatUserMapper.updateById(user);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }

        boolean avatarChanged = !Objects.equals(oldAvatar, user.getAvatar());
        boolean nicknameChanged = !Objects.equals(oldNickname, user.getNickname());
        if (avatarChanged || nicknameChanged) {
            ChatServiceRecordEntity recordUpdate = new ChatServiceRecordEntity();
            if (avatarChanged) {
                recordUpdate.setAvatar(user.getAvatar());
            }
            if (nicknameChanged) {
                recordUpdate.setNickname(user.getNickname());
            }
            chatServiceRecordMapper.update(recordUpdate,
                    new QueryWrapper<ChatServiceRecordEntity>().eq("to_user_id", id));
        }
    }

    /**
     * 批量设置分组
     * PUT /api/tenant/user/batch/group
     *
     * PHP Reference: User.php::batchGroup()
     *
     * 业务逻辑:
     * 1. 验证ids非空
     * 2. 验证group_id非空
     * 3. 批量更新用户分组
     *
     * @param ids          用户ID列表
     * @param groupId      分组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateGroup(List<Integer> ids, Integer groupId) {
        // 1. PHP: if (!$ids) return $this->fail('至少选择一个用户');
        if (ids == null || ids.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select at least one user");
        }

        // 2. PHP: if (!$groupId) return $this->fail('请选择分组');
        if (groupId == null || groupId <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select a group");
        }

        // 3. PHP: $this->services->batchUpdateGroup($ids, (int)$groupId);
        for (Integer userId : ids) {
            ChatUserEntity user = chatUserMapper.selectById(userId);
            if (user == null) {
                continue;
            }
            TenantGuard.ensureOwnedByCurrentTenant(user.getAppid(), "User does not exist");
            user.setGroupId(groupId);
            chatUserMapper.updateById(user);
        }
    }

    /**
     * 批量设置标签
     * PUT /api/tenant/user/batch/label
     *
     * PHP Reference: User.php::batchLabel()
     *
     * 业务逻辑:
     * 1. 验证ids非空
     * 2. 验证至少有label_id或un_label_id
     * 3. 添加标签：插入chat_user_label_assist记录
     * 4. 移除标签：删除chat_user_label_assist记录
     *
     * @param ids         用户ID列表
     * @param labelIds    要添加的标签ID列表
     * @param unLabelIds  要移除的标签ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateLabel(List<Integer> ids, List<Integer> labelIds, List<Integer> unLabelIds) {
        // 1. PHP: if (!$ids) return $this->fail('至少选择一个用户');
        if (ids == null || ids.isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please select at least one user");
        }

        // 2. PHP: if (!$labelId && !$unLabelId) return $this->fail('至少设置或者取消设置一个标签');
        if ((labelIds == null || labelIds.isEmpty()) && (unLabelIds == null || unLabelIds.isEmpty())) {
            throw new io.renren.crmchat.exception.CrmChatException("Please set or unset at least one tag");
        }

        // 3. PHP: $this->services->batchUpdateLabel($ids, $labelId, $unLabelId);
        for (Integer userId : ids) {
            // 验证用户存在且属于当前租户
            ChatUserEntity user = chatUserMapper.selectById(userId);
            if (user == null) {
                continue;
            }
            try {
                TenantGuard.ensureOwnedByCurrentTenant(user.getAppid(), "User does not exist");
            } catch (io.renren.crmchat.exception.CrmChatException ex) {
                continue;
            }

            // 添加标签
            if (labelIds != null && !labelIds.isEmpty()) {
                for (Integer labelId : labelIds) {
                    // 检查是否已存在
                    QueryWrapper<ChatUserLabelAssistEntity> checkWrapper = new QueryWrapper<>();
                    checkWrapper.eq("user_id", userId);
                    checkWrapper.eq("label_id", labelId);
                    Long count = chatUserLabelAssistMapper.selectCount(checkWrapper);

                    if (count == 0) {
                        ChatUserLabelAssistEntity assist = new ChatUserLabelAssistEntity();
                        assist.setUserId(userId);
                        assist.setLabelId(labelId);
                        chatUserLabelAssistMapper.insert(assist);
                    }
                }
            }

            // 移除标签
            if (unLabelIds != null && !unLabelIds.isEmpty()) {
                for (Integer unLabelId : unLabelIds) {
                    QueryWrapper<ChatUserLabelAssistEntity> deleteWrapper = new QueryWrapper<>();
                    deleteWrapper.eq("user_id", userId);
                    deleteWrapper.eq("label_id", unLabelId);
                    chatUserLabelAssistMapper.delete(deleteWrapper);
                }
            }
        }
    }

    /**
     * 获取所有标签（层级结构）
     * GET /api/tenant/user/label/all
     *
     * PHP Reference: User.php::getLavelAll()
     *
     * 业务逻辑:
     * 1. 获取所有分类及其标签（type=0）
     * 2. 转换格式：label='分类名', value=分类id, options=[标签列表]
     *
     * @return 层级标签列表
     */
    public List<Map<String, Object>> getLabelAllHierarchy() {
        // PHP: $list = $services->getLabelAll(0,$appid);
        // PHP: foreach ($list as &$item) {
        //     $item['options'] = $item['label'];
        //     foreach ($item['options'] as &$value) {
        //         $value['value'] = $value['id'];
        //     }
        //     $item['label'] = $item['name'];
        //     $item['value'] = $item['id'];
        //     unset($item['name'], $item['id']);
        // }

        // 1. 获取所有分类
        QueryWrapper<ChatUserLabelCateEntity> cateWrapper = new QueryWrapper<>();
        cateWrapper.eq("type", 0); // type=0为用户标签分类
        cateWrapper.orderByAsc("sort");
        List<ChatUserLabelCateEntity> cates = chatUserLabelCateMapper.selectList(cateWrapper);

        List<Map<String, Object>> result = new ArrayList<>();

        for (ChatUserLabelCateEntity cate : cates) {
            Map<String, Object> cateMap = new HashMap<>();
            cateMap.put("label", cate.getName());
            cateMap.put("value", cate.getId());

            // 2. 获取该分类下的所有标签
            QueryWrapper<ChatUserLabelEntity> labelWrapper = new QueryWrapper<>();
            labelWrapper.eq("cate_id", cate.getId());
            labelWrapper.orderByAsc("sort");
            List<ChatUserLabelEntity> labels = chatUserLabelMapper.selectList(labelWrapper);

            List<Map<String, Object>> options = new ArrayList<>();
            for (ChatUserLabelEntity label : labels) {
                Map<String, Object> labelMap = new HashMap<>();
                labelMap.put("label", label.getLabel());
                labelMap.put("value", label.getId());
                options.add(labelMap);
            }

            cateMap.put("options", options);
            result.add(cateMap);
        }

        return result;
    }

    /**
     * 获取所有分组
     * GET /api/tenant/user/group/all
     *
     * PHP Reference: User.php::getGroupAll()
     *
     * 业务逻辑:
     * 1. 获取所有分组（只返回id和group_name）
     *
     * @return 分组列表
     */
    public List<ChatUserGroupEntity> getGroupAll() {
        // PHP: return $this->success($services->getGroupList(['id', 'group_name'],  false,$appid));

        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.select("id", "group_name");
        wrapper.orderByAsc("sort");

        return chatUserGroupMapper.selectList(wrapper);
    }
}
