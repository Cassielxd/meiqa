package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceFeedbackMapper;
import io.renren.crmchat.entity.ChatServiceFeedbackEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Service Feedback Service - 管理员反馈管理
 * PHP Reference: ServiceFeedback.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminServiceFeedbackService {

    private final ChatServiceFeedbackMapper chatServiceFeedbackMapper;

    /**
     * 获取反馈列表
     * PHP Reference: ServiceFeedback.php::index()
     */
    public Map<String, Object> getFeedbackList(Map<String, Object> where, String appid) {
        QueryWrapper<ChatServiceFeedbackEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);

        // title搜索 - 可以是昵称或内容搜索
        String title = (String) where.get("title");
        if (title != null && !title.trim().isEmpty()) {
            wrapper.and(w -> w.like("nickname", title).or().like("content", title));
        }

        // time时间范围
        String time = (String) where.get("time");
        if (time != null && !time.trim().isEmpty()) {
            String[] times = time.split(" - ");
            if (times.length == 2) {
                wrapper.between("add_time", times[0], times[1]);
            }
        }

        // 分页
        Integer page = (Integer) where.getOrDefault("page", 1);
        Integer limit = (Integer) where.getOrDefault("limit", 20);
        Page<ChatServiceFeedbackEntity> pageObj = new Page<>(page, limit);

        wrapper.orderByDesc("id");
        IPage<ChatServiceFeedbackEntity> result = chatServiceFeedbackMapper.selectPage(pageObj, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("count", (int) result.getTotal());
        return response;
    }

    /**
     * 获取编辑表单
     * PHP Reference: ServiceFeedback.php::edit()
     */
    public Map<String, Object> getEditForm(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatServiceFeedbackEntity feedback = chatServiceFeedbackMapper.selectById(id);
        if (feedback == null || !feedback.getAppid().equals(appid)) {
            throw new CrmChatException("Feedback does not exist");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("feedback", feedback);
        // TODO: FormBuilder pattern - 返回空form_rules
        result.put("form_rules", new Object[0]);
        return result;
    }

    /**
     * 更新反馈
     * PHP Reference: ServiceFeedback.php::update()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFeedback(Integer id, Map<String, Object> data, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatServiceFeedbackEntity feedback = chatServiceFeedbackMapper.selectById(id);
        if (feedback == null || !feedback.getAppid().equals(appid)) {
            throw new CrmChatException("Feedback does not exist");
        }

        String make = (String) data.get("make");
        Integer status = (Integer) data.get("status");

        feedback.setMake(make);
        if (status != null && status > 0) {
            feedback.setStatus(status);
        }

        return chatServiceFeedbackMapper.updateById(feedback) > 0;
    }

    /**
     * 删除反馈
     * PHP Reference: ServiceFeedback.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFeedback(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatServiceFeedbackEntity feedback = chatServiceFeedbackMapper.selectById(id);
        if (feedback == null || !feedback.getAppid().equals(appid)) {
            throw new CrmChatException("Feedback does not exist");
        }

        boolean success = chatServiceFeedbackMapper.deleteById(id) > 0;
        if (!success) {
            throw new CrmChatException("Failed to delete");
        }

        return true;
    }
}
