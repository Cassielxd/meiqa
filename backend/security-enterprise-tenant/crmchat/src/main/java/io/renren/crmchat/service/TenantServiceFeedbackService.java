package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceFeedbackMapper;
import io.renren.crmchat.entity.ChatServiceFeedbackEntity;
import io.renren.crmchat.security.TenantGuard;
import io.renren.crmchat.security.TenantSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Tenant 服务反馈服务
 * PHP Reference: /app/controller/tenant/chat/ServiceFeedback.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getFeedbackList(): 获取留言列表
 *    - 支持title、time筛选
 *    - 分页查询
 * 2. getFeedbackDetail(): 获取反馈详情
 * 3. updateFeedback(): 更新反馈
 *    - 更新make备注
 *    - 更新status状态
 * 4. deleteFeedback(): 删除反馈
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantServiceFeedbackService {

    private final ChatServiceFeedbackMapper chatServiceFeedbackMapper;

    /**
     * 获取留言列表
     * GET /api/tenant/chat/feedback
     *
     * PHP Reference: ServiceFeedback.php::index()
     *
     * 业务逻辑:
     * 1. 根据appid查询
     * 2. 支持title模糊查询
     * 3. 支持time时间范围查询
     * 4. 分页查询
     *
     * @param filters      过滤条件
     * @return 分页反馈列表
     */
    public Map<String, Object> getFeedbackList(Map<String, Object> filters) {
        // PHP: $where['appid']=$appid;
        // PHP: return $this->success($this->services->getFeedbackList($where));

        // 获取当前租户appid并添加过滤条件（多租户隔离）
        String appid = TenantSecurityUtils.requireAppid();

        QueryWrapper<ChatServiceFeedbackEntity> wrapper = new QueryWrapper<>();

        // 关键：必须按appid过滤，确保租户数据隔离
        wrapper.eq("appid", appid);

        // 前端传来的title参数，实际搜索content、rela_name、phone字段
        if (filters.containsKey("title") && filters.get("title") != null && !filters.get("title").toString().isEmpty()) {
            String searchKey = filters.get("title").toString();
            wrapper.and(w -> w.like("content", searchKey)
                    .or().like("rela_name", searchKey)
                    .or().like("phone", searchKey));
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

        // 分页
        int page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        int limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceFeedbackEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceFeedbackEntity> pageResult = chatServiceFeedbackMapper.selectPage(pageObj, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("data", pageResult.getRecords());
        result.put("count", pageResult.getTotal());

        return result;
    }

    /**
     * 获取反馈详情
     * GET /api/tenant/chat/feedback/:id
     *
     * PHP Reference: ServiceFeedback.php::edit()
     *
     * @param id           反馈ID
     * @return 反馈详情
     */
    public ChatServiceFeedbackEntity getFeedbackDetail(Integer id) {
        // PHP: if (!$id) return $this->fail('缺少参数');
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatServiceFeedbackEntity feedback = chatServiceFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Feedback does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(feedback.getAppid(), "Feedback does not exist");

        return feedback;
    }

    /**
     * 更新反馈
     * PUT /api/tenant/chat/feedback/:id
     *
     * PHP Reference: ServiceFeedback.php::update()
     *
     * 业务逻辑:
     * 1. 验证反馈存在
     * 2. 更新make备注
     * 3. 更新status状态（如果提供）
     *
     * @param id           反馈ID
     * @param data         反馈数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateFeedback(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$id || !($feedInfo = $this->services->get($id))) return $this->fail('反馈内容不存在');
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatServiceFeedbackEntity feedback = chatServiceFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Feedback does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(feedback.getAppid(), "Feedback does not exist");

        // 2. PHP: $feedInfo->make = $data['make'];
        if (data.containsKey("make") && data.get("make") != null) {
            feedback.setMake(data.get("make").toString());
        }

        // 3. PHP: if ($data['status']) { $feedInfo->status = $data['status']; }
        if (data.containsKey("status") && data.get("status") != null) {
            feedback.setStatus(Integer.parseInt(data.get("status").toString()));
        }

        // PHP: $feedInfo->save();
        int result = chatServiceFeedbackMapper.updateById(feedback);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除反馈
     * DELETE /api/tenant/chat/feedback/:id
     *
     * PHP Reference: ServiceFeedback.php::delete()
     *
     * @param id           反馈ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Integer id) {
        // PHP: if (!$id) return $this->fail('缺少参数');
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatServiceFeedbackEntity feedback = chatServiceFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Feedback does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(feedback.getAppid(), "Feedback does not exist");

        // PHP: if ($this->services->delete($id)) return $this->success('删除成功');
        int result = chatServiceFeedbackMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }
}
