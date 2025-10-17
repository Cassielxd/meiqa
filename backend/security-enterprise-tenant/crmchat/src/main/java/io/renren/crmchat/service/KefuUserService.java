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
     * 获取聊天记录列表
     * PHP Reference: User.php::recordList()
     */
    public List<Map<String, Object>> getRecordList(String appid, Integer kefuUserId, String nickname, String isTourist, String labelId, String groupId) {
        // 修复: 应该查询eb_chat_service_record表,使用to_user_id字段
        // 因为客服是消息的接收方,游客是发送方
        System.out.println("=== SERVICE getRecordList ===");
        System.out.println("Parameters - appid: " + appid + ", kefuUserId: " + kefuUserId + ", nickname: " + nickname + ", isTourist: " + isTourist);

        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("to_user_id", kefuUserId);  // 修复: 改为to_user_id

        if (nickname != null && !nickname.trim().isEmpty()) {
            wrapper.like("nickname", nickname);
        }

        if (isTourist != null && !isTourist.trim().isEmpty()) {
            wrapper.eq("is_tourist", isTourist);
        }

        // 标签和分组筛选
        if (labelId != null && !labelId.trim().isEmpty()) {
            String[] labelIds = labelId.split(",");
            // TODO: 标签关联筛选
        }

        if (groupId != null && !groupId.trim().isEmpty()) {
            String[] groupIds = groupId.split(",");
            // TODO: 分组筛选
        }

        wrapper.orderByDesc("update_time");  // 修复：按更新时间降序排列，最新消息在前
        System.out.println("Executing SQL query with to_user_id=" + kefuUserId + " AND appid=" + appid);
        List<ChatServiceRecordEntity> records = chatServiceRecordMapper.selectList(wrapper);
        System.out.println("Query returned " + (records != null ? records.size() : 0) + " records from database");

        // 转换为Map（修复：确保字段名匹配前端期待）
        return records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("user_id", record.getUserId());
            map.put("to_user_id", record.getToUserId());
            map.put("nickname", record.getNickname());
            map.put("avatar", record.getAvatar());
            map.put("is_tourist", record.getIsTourist());
            map.put("add_time", record.getAddTime());

            // 前端期待 message 字段（数据库是message，Java属性是msn）
            map.put("message", record.getMsn());  // 修复：getMsn()对应数据库的message字段
            map.put("message_type", record.getMessageType());

            // 前端期待 mssage_num (三个s) 而不是 num
            map.put("mssage_num", record.getNum());  // 修复：字段名拼写

            // 前端需要 update_time 来显示时间
            map.put("update_time", record.getUpdateTime());  // 修复：添加 update_time

            // 前端需要 online 字段来显示在线状态
            map.put("online", record.getOnline() != null ? record.getOnline() : 0);  // 修复：添加 online

            return map;
        }).collect(Collectors.toList());
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
