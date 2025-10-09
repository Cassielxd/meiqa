package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceDialogueRecordMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.security.TenantQueryHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Admin Service Dialogue Record Service - 管理员对话记录管理
 * PHP Reference: ServiceDialogueRecord.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminServiceDialogueRecordService {

    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatServiceMapper chatServiceMapper;

    /**
     * 获取所有客服列表(用于筛选)
     * PHP Reference: ServiceDialogueRecord.php::kefu()
     */
    public List<Map<String, Object>> getAllKefuList(String appid) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, appid);
        wrapper.eq("status", 1).select("appid", "id", "nickname");
        List<ChatServiceEntity> list = chatServiceMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatServiceEntity service : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("appid", service.getAppid());
            item.put("id", service.getId());
            item.put("nickname", service.getNickname());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取对话用户记录列表
     * PHP Reference: ServiceDialogueRecord.php::record()
     *
     * TODO: 需要ChatServiceRecordEntity支持完整实现
     * 暂时返回空列表
     */
    public Map<String, Object> getAdminUserRecodeList(Map<String, Object> where, String appid) {
        // PHP logic:
        // - where['title'] for nickname search
        // - where['time'] for time range
        // - where['delete'] = 1 for records
        // - Returns paginated user list with message count

        // TODO: Implement when ChatServiceRecordEntity is available
        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("count", 0);
        return result;
    }

    /**
     * 获取对话记录列表
     * PHP Reference: ServiceDialogueRecord.php::index()
     */
    public List<ChatServiceDialogueRecordEntity> getDialogueRecord(Map<String, Object> where, String appid) {
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, appid);

        // kefu_id筛选 - PHP中会将kefu_id转换为user_id
        Integer kefuId = (Integer) where.get("kefu_id");
        if (kefuId != null && kefuId > 0) {
            // PHP logic: $where['kefu_id'] = $make->value($where['kefu_id'], 'user_id');
            // 即: 根据客服ID查找对应的user_id
            QueryWrapper<ChatServiceEntity> serviceWrapper = new QueryWrapper<>();
            TenantQueryHelper.applyAppid(serviceWrapper, appid);
            serviceWrapper.eq("id", kefuId).select("user_id");
            ChatServiceEntity service = chatServiceMapper.selectOne(serviceWrapper);
            if (service != null) {
                wrapper.eq("user_id", service.getUserId());
            }
        }

        // msn消息内容搜索
        String msn = (String) where.get("msn");
        if (msn != null && !msn.trim().isEmpty()) {
            wrapper.like("msn", msn);
        }

        // time时间范围
        String time = (String) where.get("time");
        if (time != null && !time.trim().isEmpty()) {
            String[] times = time.split(" - ");
            if (times.length == 2) {
                wrapper.between("create_time", times[0], times[1]);
            }
        }

        // user_id用户筛选
        Integer userId = (Integer) where.get("user_id");
        if (userId != null && userId > 0) {
            wrapper.eq("to_user_id", userId);
        }

        wrapper.orderByDesc("id");
        return chatServiceDialogueRecordMapper.selectList(wrapper);
    }
}
