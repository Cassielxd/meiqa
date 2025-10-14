package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceDialogueRecordMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatServiceRecordMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Tenant 聊天对话服务
 * PHP Reference: /app/controller/tenant/chat/ServiceDialogueRecord.php
 * PHP Reference: /app/controller/tenant/chat/Service.php (chat_user, chat_list methods)
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getDialogueRecordList(): 获取所有聊天记录
 *    - 支持kefu_id、msn、time、user_id筛选
 *    - 分页查询
 *    - 返回统计数据
 * 2. getServiceRecordList(): 获取所有聊天用户列表
 *    - 支持title、time筛选
 *    - TODO: 需要chat_service_record表
 * 3. getAllKefu(): 获取所有客服
 *    - status=1的客服列表
 * 4. getChatUserList(): 获取客服的聊天用户
 *    - 根据客服ID获取聊天过的用户列表
 * 5. getChatMessageList(): 查看对话
 *    - 获取两个用户之间的聊天记录
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantChatService {

    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatUserMapper chatUserMapper;

    /**
     * 获取所有聊天记录
     * GET /api/tenant/chat/record
     *
     * PHP Reference: ServiceDialogueRecord.php::index()
     *
     * 业务逻辑:
     * 1. 根据appid查询
     * 2. 支持kefu_id筛选（客服ID）
     * 3. 支持msn模糊查询（消息内容）
     * 4. 支持time时间范围查询
     * 5. 支持user_id查询（用户ID）
     * 6. 分页查询
     * 7. 返回统计数据
     *
     * @param filters      过滤条件
     * @return 对话记录列表和统计数据
     */
    public Map<String, Object> getDialogueRecordList(Map<String, Object> filters) {
        // PHP: $where['appid']=$appid;
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();

        // PHP: if ((int)$where['kefu_id'] === 0) { $where['kefu_id'] = ''; }
        // PHP: if ($where['kefu_id']) { $where['kefu_id'] = $make->value($where['kefu_id'], 'user_id'); }
        // PHP: when(isset($where['kefu_id']) && $where['kefu_id'] !== '', function ($query) use ($where) {
        //         $query->where(function ($query) use ($where) {
        //             $query->where('user_id', $where['kefu_id'])->whereOr('to_user_id', $where['kefu_id']);
        //         });
        //     })
        if (filters.containsKey("kefu_id") && filters.get("kefu_id") != null) {
            Integer kefuId = Integer.parseInt(filters.get("kefu_id").toString());
            if (kefuId > 0) {
                // 需要将客服ID转换为user_id
                ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
                if (kefu != null && kefu.getUserId() != null) {
                    Integer userId = kefu.getUserId();
                    wrapper.and(w -> w.eq("user_id", userId).or().eq("to_user_id", userId));
                }
            }
        }

        // PHP: when(isset($where['msn']) && $where['msn'] !== '', function ($query) use ($where) {
        //         $query->whereLike('msn', '%' . $where['msn'] . '%');
        //     })
        if (filters.containsKey("msn") && filters.get("msn") != null && !filters.get("msn").toString().trim().isEmpty()) {
            wrapper.like("msn", filters.get("msn"));
        }

        // PHP: when(isset($where['user_id']), function ($query) use ($where) {
        //         $query->where('user_id|to_user_id', $where['user_id']);
        //     })
        if (filters.containsKey("user_id") && filters.get("user_id") != null) {
            Integer userId = Integer.parseInt(filters.get("user_id").toString());
            wrapper.and(w -> w.eq("user_id", userId).or().eq("to_user_id", userId));
        }

        // TODO: time时间范围查询
        // PHP: $this->search(['time' => $where['time'] ?? ''])

        wrapper.orderByDesc("add_time");

        // 分页
        Integer page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceDialogueRecordEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceDialogueRecordEntity> pageResult = chatServiceDialogueRecordMapper.selectPage(pageObj, wrapper);

        // PHP: $data = [
        //     'record' => $service->count(),
        //     'tourist' => $userService->count(['is_tourist' => 1]),
        //     'user' => $userService->count(['is_tourist' => 0]),
        //     'count' => $this->dao->getDialogueRecord()->count()
        // ];
        // 统计数据
        Map<String, Object> data = new HashMap<>();
        data.put("record", chatServiceRecordMapper.selectCount(new QueryWrapper<ChatServiceRecordEntity>()));
        data.put("tourist", chatUserMapper.selectCount(new QueryWrapper<ChatUserEntity>().eq("is_tourist", 1)));
        data.put("user", chatUserMapper.selectCount(new QueryWrapper<ChatUserEntity>().eq("is_tourist", 0)));
        data.put("count", chatServiceDialogueRecordMapper.selectCount(new QueryWrapper<ChatServiceDialogueRecordEntity>()));

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("count", pageResult.getTotal());
        result.put("data", data);

        return result;
    }

    /**
     * 获取所有聊天用户列表
     * GET /api/tenant/chat/record/list
     *
     * PHP Reference: ServiceDialogueRecord.php::record()
     * PHP Service: ChatServiceRecordServices::getAdminUserRecodeList()
     * PHP DAO: ChatServiceRecordDao::recordModel()
     *
     * 关键逻辑:
     * 1. 必须按appid过滤（通过to_user_id关联chat_user表）
     * 2. 支持分页
     * 3. 支持nickname搜索
     * 4. delete_time为null的记录
     * 5. 返回统计数据
     *
     * @param filters      过滤条件
     * @return 聊天用户列表和统计数据
     */
    public Map<String, Object> getServiceRecordList(Map<String, Object> filters) {
        // PHP: $where['appid']=$appid; (CRITICAL!)
        String appid = io.renren.crmchat.security.TenantSecurityUtils.requireAppid();

        // PHP: $where['delete'] = 1; (whereNull('delete_time'))
        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.isNull("delete_time");

        // PHP: appid filtering through subquery
        // PHP: ->when(isset($where['appid']) && $where['appid'], function ($query) use ($where) {
        //         $query->whereIn('to_user_id', function ($query) use ($where) {
        //             $query->name('chat_user')->where('appid', $where['appid'])->field(['id']);
        //         });
        //     })
        // Java equivalent: WHERE to_user_id IN (SELECT id FROM eb_chat_user WHERE appid = ?)
        wrapper.inSql("to_user_id", "SELECT id FROM eb_chat_user WHERE appid = '" + appid + "'");

        // PHP: support both 'title' and 'nickname' parameters
        // PHP: when(isset($where['title']) && $where['title'] !== '', function ($query) use ($where) {
        //         $query->whereLike('nickname', '%' . $where['title'] . '%');
        //     })
        String searchKey = null;
        if (filters.containsKey("title") && filters.get("title") != null && !filters.get("title").toString().trim().isEmpty()) {
            searchKey = filters.get("title").toString().trim();
        } else if (filters.containsKey("nickname") && filters.get("nickname") != null && !filters.get("nickname").toString().trim().isEmpty()) {
            searchKey = filters.get("nickname").toString().trim();
        }

        if (searchKey != null) {
            wrapper.like("nickname", searchKey);
        }

        // TODO: time时间范围查询
        // PHP: $this->search(['time' => $where['time'] ?? ''])
        if (filters.containsKey("time") && filters.get("time") != null && !filters.get("time").toString().trim().isEmpty()) {
            String time = filters.get("time").toString().trim();
            log.info("Time filter requested: {}", time);
            // Common formats: 'today', 'yesterday', 'lately7', 'lately30', 'month', 'year', or date range
            // TODO: Implement time range parsing
        }

        wrapper.orderByDesc("update_time");

        // PHP: [$page, $limit] = $this->getPageValue();
        Integer page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 15;

        // 分页查询
        Page<ChatServiceRecordEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceRecordEntity> pageResult = chatServiceRecordMapper.selectPage(pageObj, wrapper);

        // PHP: Format list data with relationships
        // PHP: with(['thisUser', 'dialogueUser'])
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (ChatServiceRecordEntity item : pageResult.getRecords()) {
            Map<String, Object> itemMap = new HashMap<>();

            // Copy basic fields
            itemMap.put("id", item.getId());
            itemMap.put("user_id", item.getUserId());
            itemMap.put("to_user_id", item.getToUserId());
            itemMap.put("nickname", item.getNickname());
            itemMap.put("avatar", item.getAvatar());
            itemMap.put("is_tourist", item.getIsTourist());
            itemMap.put("online", item.getOnline());
            itemMap.put("type", item.getType());
            itemMap.put("add_time", item.getAddTime());
            itemMap.put("update_time", item.getUpdateTime());
            itemMap.put("message_type", item.getMessageType());
            itemMap.put("msn", item.getMsn());

            // Load related user data (thisUser = to_user_id)
            if (item.getToUserId() != null) {
                ChatUserEntity thisUser = chatUserMapper.selectById(item.getToUserId());
                if (thisUser != null) {
                    Map<String, Object> thisUserMap = new HashMap<>();
                    thisUserMap.put("id", thisUser.getId());
                    thisUserMap.put("nickname", thisUser.getNickname());
                    thisUserMap.put("avatar", thisUser.getAvatar());
                    thisUserMap.put("remark_nickname", thisUser.getRemarkNickname());
                    itemMap.put("thisUser", thisUserMap);
                    itemMap.put("user", thisUserMap);  // PHP uses 'user' key
                }
            }

            // Load dialogueUser (user_id)
            if (item.getUserId() != null) {
                ChatUserEntity dialogueUser = chatUserMapper.selectById(item.getUserId());
                if (dialogueUser != null) {
                    Map<String, Object> dialogueUserMap = new HashMap<>();
                    dialogueUserMap.put("id", dialogueUser.getId());
                    dialogueUserMap.put("nickname", dialogueUser.getNickname());
                    dialogueUserMap.put("avatar", dialogueUser.getAvatar());
                    itemMap.put("dialogueUser", dialogueUserMap);
                }
            }

            // Format time
            if (item.getAddTime() != null) {
                itemMap.put("_add_time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(item.getAddTime() * 1000L)));
            }

            formattedList.add(itemMap);
        }

        // PHP: 统计数据 (按appid过滤)
        // PHP: $data = [
        //     'user_count' => $userMake->count($userCountWhere),
        //     'tourist_count' => $userMake->count($touristCountWhere),
        //     'recode_count' => $this->dao->count($recordCountWhere),
        //     'dialogue_count' => $make->count($dialogueCountWhere)
        // ];
        QueryWrapper<ChatUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("appid", appid);

        QueryWrapper<ChatServiceRecordEntity> recordWrapper = new QueryWrapper<>();
        recordWrapper.isNull("delete_time");
        recordWrapper.inSql("to_user_id", "SELECT id FROM eb_chat_user WHERE appid = '" + appid + "'");

        QueryWrapper<ChatServiceDialogueRecordEntity> dialogueWrapper = new QueryWrapper<>();
        dialogueWrapper.eq("appid", appid);

        Map<String, Object> data = new HashMap<>();
        data.put("user_count", chatUserMapper.selectCount(userWrapper.clone().eq("is_tourist", 0)));
        data.put("tourist_count", chatUserMapper.selectCount(userWrapper.clone().eq("is_tourist", 1)));
        data.put("recode_count", chatServiceRecordMapper.selectCount(recordWrapper));
        data.put("dialogue_count", chatServiceDialogueRecordMapper.selectCount(dialogueWrapper));

        // PHP: return compact('list', 'count', 'data');
        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedList);
        result.put("count", pageResult.getTotal());
        result.put("data", data);

        return result;
    }

    /**
     * 获取所有客服
     * GET /api/tenant/chat/record_kefu
     *
     * PHP Reference: ServiceDialogueRecord.php::kefu()
     *
     * @return 客服列表
     */
    public List<Map<String, Object>> getAllKefu() {
        // PHP: return $this->success($services->getColumn(['status' => 1,'appid' => $appid], 'appid,id,nickname'));

        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.select("appid", "id", "nickname");

        List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatServiceEntity kefu : kefuList) {
            Map<String, Object> kefuMap = new HashMap<>();
            kefuMap.put("appid", kefu.getAppid());
            kefuMap.put("id", kefu.getId());
            kefuMap.put("nickname", kefu.getNickname());
            result.add(kefuMap);
        }

        return result;
    }

    /**
     * 获取客服的聊天用户
     * GET /api/tenant/chat/kefu/record/:id
     *
     * PHP Reference: Service.php::chat_user()
     *
     * @param kefuId       客服ID
     * @return 聊天用户列表
     */
    public List<Map<String, Object>> getChatUserList(Integer kefuId) {
        // PHP: $userId = $this->services->value(['id' => $id], 'user_id');
        // PHP: return $this->success($this->services->getChatUser((int)$userId));

        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Customer service agent does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(kefu.getAppid(), "Customer service agent does not exist");

        Integer userId = kefu.getUserId();
        if (userId == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Customer service user ID does not exist");
        }

        // PHP: getChatUser() 方法获取该客服聊天过的所有用户
        // 查询该客服作为发送者或接收者的所有对话记录，提取唯一的对方用户ID
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("user_id", userId).or().eq("to_user_id", userId));
        wrapper.groupBy("user_id", "to_user_id");
        wrapper.select("user_id", "to_user_id");

        List<ChatServiceDialogueRecordEntity> records = chatServiceDialogueRecordMapper.selectList(wrapper);

        // 提取唯一的对方用户ID
        Set<Integer> userIds = new HashSet<>();
        for (ChatServiceDialogueRecordEntity record : records) {
            if (!record.getUserId().equals(userId)) {
                userIds.add(record.getUserId());
            }
            if (!record.getToUserId().equals(userId)) {
                userIds.add(record.getToUserId());
            }
        }

        // 查询用户信息
        List<Map<String, Object>> result = new ArrayList<>();
        if (!userIds.isEmpty()) {
            List<ChatUserEntity> users = chatUserMapper.selectBatchIds(userIds);
            for (ChatUserEntity user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatar", user.getAvatar());
                result.add(userMap);
            }
        }

        return result;
    }

    /**
     * 查看对话
     * GET /api/tenant/chat/kefu/chat_list
     *
     * PHP Reference: Service.php::chat_list()
     *
     * @param filters      过滤条件
     * @return 对话消息列表
     */
    public Map<String, Object> getChatMessageList(Map<String, Object> filters) {
        // PHP: $data = $this->request->getMore([
        //     ['id', 0],
        //     ['to_user_id', 0],
        //     ['id', 0]
        // ]);
        // PHP: if ($data['id']) {
        //     CacheService::set('tenant_chat_list' . $this->tenantId, $data);
        // }
        // PHP: $data = CacheService::get('tenant_chat_list' . $this->tenantId);
        // PHP: if ($data['id']) {
        //     $where = [
        //         'chat' => [$data['id'], $data['to_user_id']],
        //     ];
        // }
        // PHP: $list = $services->getChatLogList($where);

        Integer userId = filters.containsKey("id") && filters.get("id") != null
            ? Integer.parseInt(filters.get("id").toString()) : 0;
        Integer toUserId = filters.containsKey("to_user_id") && filters.get("to_user_id") != null
            ? Integer.parseInt(filters.get("to_user_id").toString()) : 0;

        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();

        if (userId > 0 && toUserId > 0) {
            // 查询两个用户之间的对话
            wrapper.and(w -> w
                .and(w1 -> w1.eq("user_id", userId).eq("to_user_id", toUserId))
                .or(w2 -> w2.eq("user_id", toUserId).eq("to_user_id", userId))
            );
        }

        wrapper.orderByDesc("add_time");

        // 分页
        Integer page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceDialogueRecordEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceDialogueRecordEntity> pageResult = chatServiceDialogueRecordMapper.selectPage(pageObj, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("count", pageResult.getTotal());

        return result;
    }

    /**
     * 调试方法：查看数据库实际数据
     */
    public Map<String, Object> debugDatabaseData(String appid) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询 chat_user 表中该 appid 的用户
        QueryWrapper<ChatUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("appid", appid);
        List<ChatUserEntity> users = chatUserMapper.selectList(userWrapper);
        result.put("chat_user_count", users.size());
        result.put("chat_user_ids", users.stream().map(ChatUserEntity::getId).toList());

        // 2. 查询 chat_service_record 表 (所有记录，不过滤 appid)
        QueryWrapper<ChatServiceRecordEntity> allRecordsWrapper = new QueryWrapper<>();
        allRecordsWrapper.isNull("delete_time");
        long allRecordsCount = chatServiceRecordMapper.selectCount(allRecordsWrapper);
        result.put("all_chat_service_records", allRecordsCount);

        // 3. 查询该 appid 的 chat_service_record (通过 to_user_id 关联)
        QueryWrapper<ChatServiceRecordEntity> appidRecordsWrapper = new QueryWrapper<>();
        appidRecordsWrapper.isNull("delete_time");
        if (!users.isEmpty()) {
            List<Integer> userIds = users.stream().map(ChatUserEntity::getId).toList();
            appidRecordsWrapper.in("to_user_id", userIds);
        }
        List<ChatServiceRecordEntity> appidRecords = chatServiceRecordMapper.selectList(appidRecordsWrapper);
        result.put("appid_chat_service_records", appidRecords.size());

        // 4. 显示前3条记录详情
        List<Map<String, Object>> recordDetails = new ArrayList<>();
        for (int i = 0; i < Math.min(3, appidRecords.size()); i++) {
            ChatServiceRecordEntity record = appidRecords.get(i);
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", record.getId());
            detail.put("user_id", record.getUserId());
            detail.put("to_user_id", record.getToUserId());
            detail.put("nickname", record.getNickname());
            detail.put("add_time", record.getAddTime());
            recordDetails.add(detail);
        }
        result.put("sample_records", recordDetails);

        // 5. 查询 chat_service_dialogue_record 表
        QueryWrapper<ChatServiceDialogueRecordEntity> dialogueWrapper = new QueryWrapper<>();
        dialogueWrapper.eq("appid", appid);
        long dialogueCount = chatServiceDialogueRecordMapper.selectCount(dialogueWrapper);
        result.put("dialogue_records", dialogueCount);

        return result;
    }
}
