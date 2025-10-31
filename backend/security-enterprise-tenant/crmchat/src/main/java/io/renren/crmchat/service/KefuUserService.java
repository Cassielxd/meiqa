package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.*;
import io.renren.crmchat.entity.*;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Kefu User Service - 客服用户管理
 * PHP Reference: User.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class KefuUserService {

    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final ChatUserMapper chatUserMapper;
    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final ChatUserLabelMapper chatUserLabelMapper;
    private final ChatUserLabelAssistMapper chatUserLabelAssistMapper;
    private final ChatUserGroupMapper chatUserGroupMapper;

    /**
     * 根据客服服务ID获取用户ID
     * 从eb_chat_service表获取user_id字段
     */
    public Integer getKefuUserIdByServiceId(Integer serviceId, String appid) {
        if (serviceId == null) {
            return null;
        }
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", serviceId);
        wrapper.eq("appid", appid);
        ChatServiceEntity service = chatServiceMapper.selectOne(wrapper);
        return service != null ? service.getUserId() : null;
    }

    /**
     * 获取客户列表（按首字母分组）
     * PHP Reference: User.php::getUserList()
     */
    public Map<String, Object> getUserList(String appid, Integer kefuUserId, String nickname, String labelId, String groupId) {
        // TODO: 实现按首字母分组逻辑
        // PHP使用Character::groupByInitials进行分组

        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", kefuUserId);

        if (nickname != null && !nickname.trim().isEmpty()) {
            wrapper.like("nickname", nickname);
        }

        // 标签筛选
        if (labelId != null && !labelId.trim().isEmpty()) {
            String[] labelIds = labelId.split(",");
            // TODO: 通过ChatUserLabelAssist表关联筛选
        }

        // 分组筛选
        if (groupId != null && !groupId.trim().isEmpty()) {
            String[] groupIds = groupId.split(",");
            // TODO: 通过分组ID筛选
        }

        List<ChatServiceDialogueRecordEntity> records = chatServiceDialogueRecordMapper.selectList(wrapper);

        // 简化返回，实际需要按首字母分组
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        return result;
    }

    /**
     * 获取聊天记录列表（客服协同模式）
     * PHP Reference: User.php::recordList()
     *
     * 修改说明：
     * - 原逻辑：查询发给当前客服的会话（to_user_id = kefuUserId）
     * - 新逻辑：查询所有有聊天记录的用户，所有客服看到相同的用户列表
     * - 实现客服协同：任何客服都能查看和回复任何用户
     */
    public List<Map<String, Object>> getRecordList(String appid, Integer kefuUserId, String nickname, String isTourist, String labelId, String groupId) {
        System.out.println("=== SERVICE getRecordList (协同模式) ===");
        System.out.println("Parameters - appid: " + appid + ", kefuUserId: " + kefuUserId + ", nickname: " + nickname + ", isTourist: " + isTourist);

        // ✅ 修改：查询所有有聊天记录的用户（不限制客服）
        // 使用 DISTINCT 去重，避免同一个用户出现多次
        QueryWrapper<ChatServiceDialogueRecordEntity> dialogueWrapper = new QueryWrapper<>();
        dialogueWrapper.eq("appid", appid);
        dialogueWrapper.select("DISTINCT user_id, to_user_id");

        List<ChatServiceDialogueRecordEntity> dialogueRecords = chatServiceDialogueRecordMapper.selectList(dialogueWrapper);

        // 收集所有参与聊天的用户ID（排除客服）
        Set<Integer> userIds = new HashSet<>();
        for (ChatServiceDialogueRecordEntity record : dialogueRecords) {
            userIds.add(record.getUserId());
            userIds.add(record.getToUserId());
        }

        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户信息
        QueryWrapper<ChatUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("appid", appid);
        userWrapper.in("id", userIds);
        userWrapper.eq("is_delete", 0);

        // ✅ 重要：只查询非客服用户（排除 is_kefu = 1 的客服）
        // 客服列表中应该只显示游客和普通用户，不应该显示其他客服
        userWrapper.and(w -> w.isNull("is_kefu").or().eq("is_kefu", 0));

        // 过滤条件
        if (nickname != null && !nickname.trim().isEmpty()) {
            userWrapper.like("nickname", nickname);
        }

        if (isTourist != null && !isTourist.trim().isEmpty()) {
            userWrapper.eq("is_tourist", Integer.parseInt(isTourist));
        }

        // TODO: 标签和分组筛选

        List<ChatUserEntity> users = chatUserMapper.selectList(userWrapper);
        System.out.println("Found " + users.size() + " users with chat records (excluding kefu)");

        // ✅ 性能优化：批量查询所有客服信息，避免 N+1 查询问题
        // 1. 先收集所有需要查询的客服ID
        Set<Integer> kefuUserIds = new HashSet<>();
        for (ChatUserEntity user : users) {
            QueryWrapper<ChatServiceRecordEntity> tempWrapper = new QueryWrapper<>();
            tempWrapper.eq("appid", appid);
            tempWrapper.eq("user_id", user.getId());
            tempWrapper.eq("to_user_id", kefuUserId);
            ChatServiceRecordEntity tempRecord = chatServiceRecordMapper.selectOne(tempWrapper);
            if (tempRecord != null && tempRecord.getToUserId() != null) {
                kefuUserIds.add(tempRecord.getToUserId());
            }
        }

        // 2. 批量查询所有客服信息，构建 Map 缓存
        Map<Integer, String> kefuNicknameMap = new HashMap<>();
        if (!kefuUserIds.isEmpty()) {
            QueryWrapper<ChatUserEntity> kefuWrapper = new QueryWrapper<>();
            kefuWrapper.in("id", kefuUserIds);
            List<ChatUserEntity> kefuUsers = chatUserMapper.selectList(kefuWrapper);
            for (ChatUserEntity kefuUser : kefuUsers) {
                kefuNicknameMap.put(kefuUser.getId(), kefuUser.getNickname());
            }
        }

        // 为每个用户构建会话摘要
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();

            // ✅ 修改：保持与 chat_service_record 表一致的数据结构
            // user_id = 游客ID（发送人）
            // to_user_id = 客服ID（接收人）
            // 这样前端可以用 user_id 来判断是否同一个游客
            map.put("user_id", user.getId());  // 游客ID
            map.put("to_user_id", kefuUserId);  // 当前客服ID
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getAvatar());
            map.put("is_tourist", user.getIsTourist() != null ? user.getIsTourist() : 0);
            map.put("is_kefu", user.getIsKefu() != null ? user.getIsKefu() : 0);
            map.put("online", user.getOnline() != null ? user.getOnline() : 0);

            // 查询最新消息
            QueryWrapper<ChatServiceDialogueRecordEntity> msgWrapper = new QueryWrapper<>();
            msgWrapper.eq("appid", appid);
            msgWrapper.and(w -> w.eq("user_id", user.getId()).or().eq("to_user_id", user.getId()));
            msgWrapper.orderByDesc("add_time");
            msgWrapper.last("LIMIT 1");

            ChatServiceDialogueRecordEntity lastMsg = chatServiceDialogueRecordMapper.selectOne(msgWrapper);
            if (lastMsg != null) {
                map.put("message", lastMsg.getMsn());
                map.put("message_type", lastMsg.getMsnType());
                map.put("add_time", lastMsg.getAddTime());
                map.put("update_time", lastMsg.getAddTime());  // 使用消息时间作为更新时间
            } else {
                map.put("message", "");
                map.put("message_type", 1);
                map.put("add_time", 0);
                map.put("update_time", 0);
            }

            // 查询未读数（针对当前客服）
            // ✅ 修改：chat_service_record 表结构是 user_id=游客, to_user_id=客服
            QueryWrapper<ChatServiceRecordEntity> unreadWrapper = new QueryWrapper<>();
            unreadWrapper.eq("appid", appid);
            unreadWrapper.eq("user_id", user.getId());  // 游客ID
            unreadWrapper.eq("to_user_id", kefuUserId);  // 当前客服ID

            ChatServiceRecordEntity recordEntity = chatServiceRecordMapper.selectOne(unreadWrapper);
            map.put("mssage_num", recordEntity != null && recordEntity.getNum() != null ? recordEntity.getNum() : 0);

            // ✅ 添加 id 字段（如果存在 chat_service_record 记录）
            // ✅ 添加 is_my_customer 字段（标识是否属于当前客服）
            // ✅ 添加 service_nickname 字段（该用户对应的客服昵称）
            if (recordEntity != null) {
                map.put("id", recordEntity.getId());
                map.put("is_my_customer", 1);  // 有记录 = 属于当前客服

                // ✅ 从缓存 Map 中获取客服昵称（性能优化）
                Integer serviceUserId = recordEntity.getToUserId();
                String serviceNickname = kefuNicknameMap.getOrDefault(serviceUserId, "");
                map.put("service_nickname", serviceNickname);
            } else {
                // 如果没有记录，使用游客ID作为临时ID（前端需要一个唯一标识）
                map.put("id", user.getId());
                map.put("is_my_customer", 0);  // 没有记录 = 不属于当前客服
                map.put("service_nickname", "");  // 没有分配客服
            }

            return map;
        }).sorted((a, b) -> {
            // ✅ 多级排序：
            // 1. 优先显示属于当前客服的用户
            // 2. 其次按未读消息数排序（有未读的排前面）
            // 3. 最后按最新消息时间排序

            Integer isMyA = (Integer) a.get("is_my_customer");
            Integer isMyB = (Integer) b.get("is_my_customer");

            // 第一优先级：属于当前客服的排前面
            if (!isMyA.equals(isMyB)) {
                return isMyB.compareTo(isMyA);  // 1 排在 0 前面
            }

            // 第二优先级：未读消息数（有未读的排前面）
            Integer unreadA = (Integer) a.get("mssage_num");
            Integer unreadB = (Integer) b.get("mssage_num");
            if (!unreadA.equals(unreadB)) {
                return unreadB.compareTo(unreadA);  // 未读数多的排前面
            }

            // 第三优先级：最新消息时间
            Integer timeA = (Integer) a.get("update_time");
            Integer timeB = (Integer) b.get("update_time");
            return timeB.compareTo(timeA);
        }).collect(Collectors.toList());

        System.out.println("Returning " + result.size() + " conversation records");
        return result;
    }

    /**
     * 获取用户信息
     * PHP Reference: User.php::userInfo()
     */
    public Map<String, Object> getUserInfo(Integer userId) {
        ChatUserEntity user = chatUserMapper.selectById(userId);
        if (user == null) {
            throw new CrmChatException("User does not exist");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("to_user_id", user.getId());  // 兼容前端取值错误
        result.put("appid", user.getAppid());
        result.put("nickname", user.getNickname());
        result.put("remark_nickname", user.getRemarkNickname());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        result.put("sex", user.getSex());
        result.put("remarks", user.getRemarks());
        result.put("group_id", user.getGroupId());
        result.put("is_tourist", user.getIsTourist());
        // result.put("online", user.getOnline());  // TODO: online字段

        // ✅ IP和地理位置信息
        result.put("last_ip", user.getLastIp() != null ? user.getLastIp() : "");
        result.put("country", user.getCountry() != null ? user.getCountry() : "");
        result.put("region", user.getRegion() != null ? user.getRegion() : "");
        result.put("city", user.getCity() != null ? user.getCity() : "");
        result.put("isp", user.getIsp() != null ? user.getIsp() : "");

        // 组合地理位置文本
        StringBuilder locationBuilder = new StringBuilder();
        if (user.getCountry() != null && !user.getCountry().isEmpty()) {
            locationBuilder.append(user.getCountry());
        }
        if (user.getRegion() != null && !user.getRegion().isEmpty()) {
            if (locationBuilder.length() > 0) locationBuilder.append("-");
            locationBuilder.append(user.getRegion());
        }
        if (user.getCity() != null && !user.getCity().isEmpty()) {
            if (locationBuilder.length() > 0) locationBuilder.append("-");
            locationBuilder.append(user.getCity());
        }
        result.put("location", locationBuilder.toString());

        // 获取用户标签
        List<ChatUserLabelAssistEntity> labelAssists = chatUserLabelAssistMapper.selectList(
                new QueryWrapper<ChatUserLabelAssistEntity>().eq("user_id", userId)
        );
        List<Integer> labelIds = labelAssists.stream()
                .map(ChatUserLabelAssistEntity::getLabelId)
                .collect(Collectors.toList());

        if (!labelIds.isEmpty()) {
            List<ChatUserLabelEntity> labels = chatUserLabelMapper.selectBatchIds(labelIds);
            result.put("label", labels);
        } else {
            result.put("label", new ArrayList<>());
        }

        return result;
    }

    /**
     * 获取用户标签
     * PHP Reference: User.php::getUserLabel() -> ChatUserLabelCateServices::getLabelAll()
     *
     * 业务逻辑:
     * 1. 查询所有标签分类(type=0)
     * 2. 查询每个分类下的标签
     * 3. 统计每个标签的用户数量
     * 4. 如果传入用户ID，标记该用户已有的标签为disabled=true
     *
     * @param id 用户ID，如果为0或null则不标记已有标签
     * @return 标签分类列表（每个分类包含标签列表）
     */
    public List<Map<String, Object>> getUserLabel(Integer id) {
        // 1. 获取所有标签分类 (type=0表示用户标签分类)
        QueryWrapper<ChatUserLabelCateEntity> cateWrapper = new QueryWrapper<>();
        cateWrapper.eq("type", 0);
        cateWrapper.orderByAsc("sort");
        List<ChatUserLabelCateEntity> categories = chatUserLabelCateMapper.selectList(cateWrapper);

        // 2. 如果传入用户ID，获取该用户已有的标签ID列表
        Set<Integer> userLabelIds = new HashSet<>();
        if (id != null && id > 0) {
            List<ChatUserLabelAssistEntity> userLabels = chatUserLabelAssistMapper.selectList(
                new QueryWrapper<ChatUserLabelAssistEntity>().eq("user_id", id)
            );
            userLabelIds = userLabels.stream()
                .map(ChatUserLabelAssistEntity::getLabelId)
                .collect(Collectors.toSet());
        }

        // 3. 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatUserLabelCateEntity cate : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cate.getId());
            item.put("name", cate.getName());

            // 获取分类下的所有标签
            QueryWrapper<ChatUserLabelEntity> labelWrapper = new QueryWrapper<>();
            labelWrapper.eq("cate_id", cate.getId());
            labelWrapper.orderByAsc("sort");
            List<ChatUserLabelEntity> labels = chatUserLabelMapper.selectList(labelWrapper);

            // 转换为Map列表，添加disabled标记和用户数量统计
            List<Map<String, Object>> labelList = new ArrayList<>();
            for (ChatUserLabelEntity label : labels) {
                Map<String, Object> labelMap = new HashMap<>();
                labelMap.put("id", label.getId());
                labelMap.put("label", label.getLabel());
                labelMap.put("cate_id", label.getCateId());
                labelMap.put("sort", label.getSort());
                labelMap.put("user_id", label.getUserId());
                labelMap.put("appid", label.getAppid());

                // 标记该用户是否已有此标签
                boolean hasLabel = userLabelIds.contains(label.getId());
                labelMap.put("disabled", hasLabel);

                // 统计使用此标签的用户数量
                Long countUser = chatUserLabelAssistMapper.selectCount(
                    new QueryWrapper<ChatUserLabelAssistEntity>().eq("label_id", label.getId())
                );
                labelMap.put("count_user", countUser);

                labelList.add(labelMap);
            }

            item.put("label", labelList);
            result.add(item);
        }

        return result;
    }

    /**
     * 获取用户分组
     * PHP Reference: User.php::getUserGroup()
     */
    public List<Map<String, Object>> getUserGroup() {
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        // 表中没有is_show和sort字段,直接查询所有分组
        wrapper.orderByAsc("id");
        List<ChatUserGroupEntity> groups = chatUserGroupMapper.selectList(wrapper);

        return groups.stream().map(group -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("group_name", group.getGroupName());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 设置用户分组
     * PHP Reference: User.php::setUserGroup()
     */
    @Transactional(rollbackFor = Exception.class)
    public void setUserGroup(Integer userId, Integer groupId) {
        // 检查分组是否存在
        ChatUserGroupEntity group = chatUserGroupMapper.selectById(groupId);
        if (group == null) {
            throw new CrmChatException("The member tag to add does not exist");
        }

        // 检查用户是否存在
        ChatUserEntity user = chatUserMapper.selectById(userId);
        if (user == null) {
            throw new CrmChatException("User does not exist");
        }

        // 检查是否已有此分组
        if (user.getGroupId() != null && user.getGroupId().equals(groupId)) {
            throw new CrmChatException("Already has this group");
        }

        // 更新用户分组
        user.setGroupId(groupId);
        chatUserMapper.updateById(user);
    }

    /**
     * 设置用户标签
     * PHP Reference: User.php::setUserLabel()
     */
    @Transactional(rollbackFor = Exception.class)
    public void setUserLabel(Integer userId, List<Integer> labelIds, List<Integer> unLabelIds) {
        // 添加标签
        if (labelIds != null && !labelIds.isEmpty()) {
            for (Integer labelId : labelIds) {
                // 检查是否已存在
                long count = chatUserLabelAssistMapper.selectCount(
                        new QueryWrapper<ChatUserLabelAssistEntity>()
                                .eq("user_id", userId)
                                .eq("label_id", labelId)
                );

                if (count == 0) {
                    ChatUserLabelAssistEntity assist = new ChatUserLabelAssistEntity();
                    assist.setUserId(userId);
                    assist.setLabelId(labelId);
                    chatUserLabelAssistMapper.insert(assist);
                }
            }
        }

        // 删除标签
        if (unLabelIds != null && !unLabelIds.isEmpty()) {
            chatUserLabelAssistMapper.delete(
                    new QueryWrapper<ChatUserLabelAssistEntity>()
                            .eq("user_id", userId)
                            .in("label_id", unLabelIds)
            );
        }
    }

    /**
     * 修改用户信息
     * PHP Reference: User.php::updateUser()
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Integer userId, Map<String, Object> data) {
        ChatUserEntity user = chatUserMapper.selectById(userId);
        if (user == null) {
            throw new CrmChatException("User does not exist");
        }

        String phone = (String) data.get("phone");
        if (phone != null && !phone.trim().isEmpty()) {
            // 手机号验证
            if (!phone.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$")) {
                throw new CrmChatException("Please enter a valid phone number");
            }
            user.setPhone(phone);
            user.setIsTourist(0);  // 有手机号不是游客

            // TODO: 同步更新ChatServiceDialogueRecord表
            // chatServiceDialogueRecordMapper.update...
        }

        String nickname = (String) data.get("nickname");
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }

        String remarkNickname = (String) data.get("remark_nickname");
        if (remarkNickname != null) {
            user.setRemarkNickname(remarkNickname);
        }

        Integer sex = (Integer) data.get("sex");
        if (sex != null) {
            user.setSex(sex);
        }

        String remarks = (String) data.get("remarks");
        if (remarks != null) {
            user.setRemarks(remarks);
        }

        chatUserMapper.updateById(user);
    }
}
