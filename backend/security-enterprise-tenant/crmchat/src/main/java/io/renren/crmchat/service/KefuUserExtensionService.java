package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.*;
import io.renren.crmchat.entity.*;
import io.renren.crmchat.security.TenantGuard;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.security.TenantQueryHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kefu User Extension Service - 客服用户交互扩展功能服务
 * PHP Reference: /app/controller/kefu/User.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getRecordAllList(): 获取所有聊天用户列表
 * 2. deleteRecordUser(): 删除聊天记录用户（软删除）
 * 3. deleteUserLabel(): 删除用户标签关联
 * 4. updateServiceClientId(): 更新客服client_id（WebSocket连接）
 * 5. blockUser(): 拉黑用户（删除所有相关记录）
 * 6. saveChatLog(): 保存聊天日志
 * 7. getComplainCategories(): 获取投诉分类
 * 8. submitComplain(): 提交投诉
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuUserExtensionService {

    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatUserLabelAssistMapper chatUserLabelAssistMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatUserMapper chatUserMapper;
    private final CategoryMapper categoryMapper;
    private final ChatComplainMapper chatComplainMapper;
    private final AutoBadgeService autoBadgeService;

    /**
     * 获取聊天用户列表（支持is_tourist筛选）
     * GET /api/kefu/user/record
     *
     * PHP Reference: User.php::recordList() -> ChatServiceRecordServices::getServiceList()
     * PHP实现: eb_chat_service_record表中 user_id=游客, to_user_id=客服
     * 客服查询自己的聊天列表时，应该查询 to_user_id = kefuUserId
     *
     * 业务逻辑:
     * 1. 查询当前客服的聊天记录用户
     * 2. 支持按nickname模糊搜索
     * 3. 支持按is_tourist筛选（""=全部, "0"=注册用户, "1"=游客）
     * 4. 按ID倒序排列
     *
     * @param kefuUserId   当前客服的user_id
     * @param nickname     用户昵称（可选，模糊查询）
     * @param isTourist    是否游客（""=全部, "0"=注册用户, "1"=游客）
     * @param appid        租户ID
     * @return 聊天用户列表
     */
    public List<ChatServiceRecordEntity> getRecordList(Integer kefuUserId, String nickname, String isTourist, String appid) {
        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        // 根据PHP实现: eb_chat_service_record表中 user_id=游客, to_user_id=客服
        // 客服查询自己的聊天用户列表时，查询 to_user_id = kefuUserId
        wrapper.eq("to_user_id", kefuUserId);  // to_user_id = 客服 (per PHP implementation)
        wrapper.eq("appid", appid);

        if (nickname != null && !nickname.trim().isEmpty()) {
            wrapper.like("nickname", nickname);
        }

        // PHP逻辑: if ($value !== '') { $query->where('is_tourist', $value); }
        // 如果is_tourist参数不为空字符串，则添加筛选条件
        if (isTourist != null && !isTourist.trim().isEmpty()) {
            wrapper.eq("is_tourist", Integer.parseInt(isTourist));
        }

        wrapper.orderByDesc("id");

        return chatServiceRecordMapper.selectList(wrapper);
    }

    /**
     * 获取所有聊天用户列表
     * GET /api/kefu/user/record/all
     *
     * PHP Reference: User.php::recordAllList() -> ChatServiceRecordServices::getRecordAllList()
     * PHP实现: eb_chat_service_record表中 user_id=游客, to_user_id=客服
     * 客服查询自己的聊天列表时，应该查询 to_user_id = kefuUserId
     *
     * 业务逻辑:
     * 1. 查询当前客服的所有聊天记录用户
     * 2. 支持按nickname模糊搜索
     * 3. 按ID倒序排列
     *
     * @param kefuUserId   当前客服的user_id
     * @param nickname     用户昵称（可选，模糊查询）
     * @param appid        租户ID
     * @return 聊天用户列表
     */
    public List<ChatServiceRecordEntity> getRecordAllList(Integer kefuUserId, String nickname, String appid) {
        return getRecordList(kefuUserId, nickname, "", appid);  // 复用新方法，is_tourist=""表示查询全部
    }

    /**
     * 删除聊天记录用户
     * DELETE /api/kefu/user/record/:id
     *
     * PHP Reference: User.php::deleteRecordUser()
     *
     * 业务逻辑:
     * 1. 软删除：设置delete_time字段
     * 2. 不做物理删除
     *
     * @param recordId ChatServiceRecord的ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecordUser(Integer recordId) {
        if (recordId == null || recordId <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatServiceRecordEntity record = chatServiceRecordMapper.selectById(recordId);
        if (record == null) {
            throw new CrmChatException("Record does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(record.getAppid(), "Record does not exist");

        // 软删除：设置delete_time（PHP中使用date('Y-m-d H:i:s')）
        record.setDeleteTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceRecordMapper.updateById(record);
        if (result <= 0) {
            throw new CrmChatException("Failed to delete");
        }

        log.info("删除聊天记录用户成功: recordId={}", recordId);
    }

    /**
     * 删除用户标签
     * DELETE /api/kefu/user/:userId/label/:labelId
     *
     * PHP Reference: User.php::delUserLabel()
     *
     * 业务逻辑:
     * 1. 从ChatUserLabelAssist表删除用户和标签的关联关系
     * 2. 不是删除标签本身，而是删除用户的标签关联
     *
     * @param userId  用户ID
     * @param labelId 标签ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserLabel(Integer userId, Integer labelId) {
        if (userId == null || userId <= 0) {
            throw new CrmChatException("Missing user ID parameter");
        }
        if (labelId == null || labelId <= 0) {
            throw new CrmChatException("Missing label ID parameter");
        }

        QueryWrapper<ChatUserLabelAssistEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("label_id", labelId);

        int result = chatUserLabelAssistMapper.delete(wrapper);
        if (result <= 0) {
            throw new CrmChatException("Failed to delete");
        }

        log.info("删除用户标签成功: userId={}, labelId={}", userId, labelId);
    }

    /**
     * 更新客服client_id
     * PUT /api/kefu/service
     *
     * PHP Reference: User.php::updateService()
     *
     * 业务逻辑:
     * 1. 先清空所有客服的相同client_id（保证client_id唯一性）
     * 2. 更新当前客服的client_id（WebSocket连接标识）
     * 3. 触发 AutoBadgeService 刷新徽章
     *
     * @param kefuId   当前客服ID
     * @param clientId WebSocket客户端ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateServiceClientId(Integer kefuUserId, String appid, String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new CrmChatException("client_id cannot be empty");
        }

        // 1. 先清空所有客服的相同client_id
        ChatServiceEntity clearOldClientId = new ChatServiceEntity();
        clearOldClientId.setClientId("");

        QueryWrapper<ChatServiceEntity> clearWrapper = new QueryWrapper<>();
        clearWrapper.eq("client_id", clientId);
        chatServiceMapper.update(clearOldClientId, clearWrapper);

        // 2. 更新当前客服的client_id
        QueryWrapper<ChatServiceEntity> currentWrapper = new QueryWrapper<>();
        currentWrapper.eq("user_id", kefuUserId);
        TenantQueryHelper.applyAppid(currentWrapper, appid);
        ChatServiceEntity currentKefu = chatServiceMapper.selectOne(currentWrapper);
        if (currentKefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        currentKefu.setClientId(clientId);
        currentKefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceMapper.updateById(currentKefu);
        if (result <= 0) {
            throw new CrmChatException("Failed to update");
        }

        autoBadgeService.dispatch(currentKefu.getUserId(), 0, currentKefu.getAppid());

        log.info("更新客服client_id成功: kefuUserId={}, clientId={}", kefuUserId, clientId);
    }

    /**
     * 拉黑用户
     * POST /api/kefu/user/:userId/block
     *
     * PHP Reference: User.php::status()
     *
     * 业务逻辑:
     * 1. 在事务中删除以下所有相关记录:
     *    - ChatServiceRecord（to_user_id = userId）
     *    - ChatServiceRecord（user_id = userId）
     *    - ChatServiceDialogueRecord（to_user_id = userId）
     *    - ChatUser（userId）
     * 2. 这是硬删除，不是软删除
     * 3. 命名为"拉黑"，实际是彻底删除用户及所有相关记录
     *
     * @param userId 要拉黑的用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void blockUser(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new CrmChatException("Missing user ID");
        }

        ChatUserEntity targetUser = chatUserMapper.selectById(userId);
        if (targetUser == null) {
            throw new CrmChatException("User does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(targetUser.getAppid(), "User does not exist");

        // 1. 删除ChatServiceRecord（to_user_id = userId）
        QueryWrapper<ChatServiceRecordEntity> recordWrapper1 = new QueryWrapper<>();
        recordWrapper1.eq("to_user_id", userId);
        chatServiceRecordMapper.delete(recordWrapper1);

        // 2. 删除ChatServiceRecord（user_id = userId）
        QueryWrapper<ChatServiceRecordEntity> recordWrapper2 = new QueryWrapper<>();
        recordWrapper2.eq("user_id", userId);
        chatServiceRecordMapper.delete(recordWrapper2);

        // 3. 删除ChatServiceDialogueRecord（to_user_id = userId）
        QueryWrapper<ChatServiceDialogueRecordEntity> dialogueWrapper = new QueryWrapper<>();
        dialogueWrapper.eq("to_user_id", userId);
        chatServiceDialogueRecordMapper.delete(dialogueWrapper);

        // 4. 删除ChatUser
        int result = chatUserMapper.deleteById(userId);
        if (result <= 0) {
            throw new CrmChatException("Failed to block: user does not exist");
        }

        log.info("拉黑用户成功: userId={}", userId);
    }

    /**
     * 保存聊天日志
     * POST /api/kefu/chat/log
     *
     * PHP Reference: User.php::savelog()
     *
     * 业务逻辑:
     * 1. 保存客服发送的聊天消息到ChatServiceDialogueRecord表
     * 2. 必填字段：to_user_id, msn（消息内容）
     * 3. 可选字段：other（额外信息JSON）, type, is_send, msn_type
     * 4. 返回保存后的记录（附带客服的nickname和avatar）
     *
     * @param data         聊天日志数据
     * @param kefuId       当前客服ID
         * @return 保存后的聊天记录
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveChatLog(Map<String, Object> data, Integer kefuUserId, String appid) {
        // 1. 获取客服信息
        QueryWrapper<ChatServiceEntity> kefuWrapper = new QueryWrapper<>();
        kefuWrapper.eq("user_id", kefuUserId);
        TenantQueryHelper.applyAppid(kefuWrapper, appid);
        ChatServiceEntity kefu = chatServiceMapper.selectOne(kefuWrapper);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(kefu.getAppid(), "Customer service agent does not exist");

        // 2. 验证必填字段
        if (!data.containsKey("to_user_id") || data.get("to_user_id") == null) {
            throw new CrmChatException("Missing parameter: to_user_id");
        }
        if (!data.containsKey("msn") || data.get("msn") == null || data.get("msn").toString().trim().isEmpty()) {
            throw new CrmChatException("Missing parameter: msn");
        }

        Integer toUserId = Integer.parseInt(data.get("to_user_id").toString());
        String msn = data.get("msn").toString();

        ChatUserEntity toUser = chatUserMapper.selectById(toUserId);
        if (toUser == null) {
            throw new CrmChatException("User does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(toUser.getAppid(), "User does not exist");

        // 3. 创建聊天记录
        ChatServiceDialogueRecordEntity record = new ChatServiceDialogueRecordEntity();
        record.setUserId(kefuUserId);  // 发送人是客服的user_id
        record.setToUserId(toUserId);  // 接收人
        record.setMsn(msn);
        record.setIsTourist(toUser.getIsTourist());
        record.setAppid(appid);

        // 可选字段
        if (data.containsKey("other") && data.get("other") != null) {
            // PHP中other是数组，Java中可能需要转成JSON字符串
            record.setOther(data.get("other").toString());
        }
        if (data.containsKey("type") && data.get("type") != null) {
            record.setType(Integer.parseInt(data.get("type").toString()));
        }
        if (data.containsKey("msn_type") && data.get("msn_type") != null) {
            record.setMsnType(Integer.parseInt(data.get("msn_type").toString()));
        }
        // 注意：is_send字段在表中不存在，已忽略

        record.setAddTime((int) (System.currentTimeMillis() / 1000));

        // 4. 保存记录
        int result = chatServiceDialogueRecordMapper.insert(record);
        if (result <= 0) {
            throw new CrmChatException("Failed to insert");
        }

        // 5. 返回记录（附带客服信息）
        Map<String, Object> response = new HashMap<>();
        response.put("id", record.getId());
        response.put("appid", record.getAppid());
        response.put("user_id", record.getUserId());
        response.put("to_user_id", record.getToUserId());
        response.put("msn", record.getMsn());
        response.put("msn_type", record.getMsnType());
        response.put("type", record.getType());
        response.put("other", record.getOther());
        response.put("add_time", record.getAddTime());
        response.put("nickname", kefu.getNickname());
        response.put("avatar", kefu.getAvatar());

        // PHP中的格式化时间（Java可选）
        Timestamp timestamp = new Timestamp(record.getAddTime() * 1000L);
        response.put("_add_time", timestamp.toString());

        log.info("保存聊天日志成功: kefuUserId={}, toUserId={}, recordId={}", kefuUserId, toUserId, record.getId());
        return response;
    }

    /**
     * 获取投诉分类
     * GET /api/kefu/complain/categories
     *
     * PHP Reference: User.php::getComplainList() -> CategoryServices::getComplainList()
     *
     * 业务逻辑:
     * 1. 从Category表查询type=2的分类（投诉分类）
     * 2. 转成树形结构（PHP中使用get_tree_children辅助函数）
     * 3. 简化实现：返回所有投诉分类，不做树形转换（前端处理）
     *
     * @return 投诉分类列表
     */
    public List<CategoryEntity> getComplainCategories() {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 2);  // 投诉分类
        wrapper.orderByAsc("sort");
        wrapper.orderByAsc("id");

        return categoryMapper.selectList(wrapper);
    }

    /**
     * 提交投诉
     * POST /api/kefu/complain
     *
     * PHP Reference: User.php::complain()
     *
     * 业务逻辑:
     * 1. 保存投诉记录到ChatComplain表
     * 2. cate_id是数组，使用"/"分隔（例如: "1/3" 表示一级分类1下的二级分类3）
     * 3. content: 投诉内容
     * 4. user_id: 被投诉的用户ID
     *
     * @param data 投诉数据 {content, user_id, cate_id数组}
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitComplain(Map<String, Object> data) {
        // 1. 验证必填字段
        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter complaint content");
        }
        if (!data.containsKey("user_id") || data.get("user_id") == null) {
            throw new CrmChatException("Missing user ID");
        }
        if (!data.containsKey("cate_id") || data.get("cate_id") == null) {
            throw new CrmChatException("Please select complaint category");
        }

        String content = data.get("content").toString();
        Integer userId = Integer.parseInt(data.get("user_id").toString());

        // 2. 处理cate_id（PHP中是数组，使用implode('/', cate_id)）
        String cateId;
        Object cateIdObj = data.get("cate_id");
        if (cateIdObj instanceof List) {
            List<?> cateIdList = (List<?>) cateIdObj;
            cateId = String.join("/", cateIdList.stream()
                    .map(Object::toString)
                    .toArray(String[]::new));
        } else {
            cateId = cateIdObj.toString();
        }

        // 3. 创建投诉记录
        ChatComplainEntity complain = new ChatComplainEntity();
        complain.setContent(content);
        complain.setUserId(userId);
        complain.setCateId(cateId);
        complain.setCreateTime(new Timestamp(System.currentTimeMillis()));

        int result = chatComplainMapper.insert(complain);
        if (result <= 0) {
            throw new CrmChatException("Failed to submit complaint");
        }

        log.info("投诉提交成功: userId={}, cateId={}", userId, cateId);
    }
}
