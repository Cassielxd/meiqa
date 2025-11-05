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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter[] RANGE_DATE_TIME_FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };
    private static final DateTimeFormatter[] RANGE_DATE_FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

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

        // 前端传来的title参数，实际搜索rela_name、phone、content、user_id字段
        if (filters.containsKey("title") && filters.get("title") != null && !filters.get("title").toString().trim().isEmpty()) {
            String searchKey = filters.get("title").toString().trim();
            wrapper.and(w -> w.like("rela_name", searchKey)
                    .or().like("phone", searchKey)
                    .or().like("content", searchKey)
                    .or().like("user_id", searchKey));
        }

        if (filters.containsKey("time") && filters.get("time") != null && !filters.get("time").toString().trim().isEmpty()) {
            applyTimeFilter(wrapper, filters.get("time").toString().trim());
        }

        wrapper.orderByDesc("id");

        // 分页
        int page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        int limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceFeedbackEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceFeedbackEntity> pageResult = chatServiceFeedbackMapper.selectPage(pageObj, wrapper);

        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (ChatServiceFeedbackEntity entity : pageResult.getRecords()) {
            formattedList.add(formatFeedbackRecord(entity));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", formattedList);
        result.put("count", (int) pageResult.getTotal());

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
     * 获取反馈处理表单配置
     * GET /api/tenant/chat/feedback/{id}/edit
     */
    public Map<String, Object> getEditForm(Integer id) {
        ChatServiceFeedbackEntity feedback = getFeedbackDetail(id);

        List<Map<String, Object>> rules = new ArrayList<>();

        Map<String, Object> makeRule = new HashMap<>();
        makeRule.put("type", "textarea");
        makeRule.put("field", "make");
        makeRule.put("title", "备注");
        makeRule.put("value", feedback.getMake() != null ? feedback.getMake() : "");
        Map<String, Object> makeProps = new HashMap<>();
        Map<String, Object> autosize = new HashMap<>();
        autosize.put("minRows", 3);
        autosize.put("maxRows", 6);
        makeProps.put("autosize", autosize);
        makeRule.put("props", makeProps);
        rules.add(makeRule);

        Integer status = feedback.getStatus();
        if (status == null || status == 0) {
            Map<String, Object> statusRule = new HashMap<>();
            statusRule.put("type", "radio");
            statusRule.put("field", "status");
            statusRule.put("title", "状态");
            statusRule.put("value", status != null ? status : 0);

            List<Map<String, Object>> options = new ArrayList<>();
            Map<String, Object> processed = new HashMap<>();
            processed.put("label", "已处理");
            processed.put("value", 1);
            Map<String, Object> unprocessed = new HashMap<>();
            unprocessed.put("label", "未处理");
            unprocessed.put("value", 0);
            options.add(processed);
            options.add(unprocessed);
            statusRule.put("options", options);

            rules.add(statusRule);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rules", rules);
        result.put("title", (status != null && status == 1) ? "备注" : "处理");
        result.put("action", "/chat/feedback/" + feedback.getId());
        result.put("method", "PUT");
        result.put("info", "");
        result.put("status", true);

        return result;
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
            try {
                feedback.setStatus(Integer.parseInt(data.get("status").toString()));
            } catch (NumberFormatException ex) {
                log.warn("Invalid status value: {}", data.get("status"));
            }
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

    private void applyTimeFilter(QueryWrapper<ChatServiceFeedbackEntity> wrapper, String time) {
        long start = 0L;
        long end = 0L;
        LocalDate today = LocalDate.now();
        switch (time) {
            case "today" -> {
                start = toEpochSeconds(today.atStartOfDay());
                end = toEpochSeconds(today.atTime(23, 59, 59));
            }
            case "yesterday" -> {
                LocalDate date = today.minusDays(1);
                start = toEpochSeconds(date.atStartOfDay());
                end = toEpochSeconds(date.atTime(23, 59, 59));
            }
            case "lately7" -> {
                LocalDate date = today.minusDays(6);
                start = toEpochSeconds(date.atStartOfDay());
                end = toEpochSeconds(today.atTime(23, 59, 59));
            }
            case "lately30" -> {
                LocalDate date = today.minusDays(29);
                start = toEpochSeconds(date.atStartOfDay());
                end = toEpochSeconds(today.atTime(23, 59, 59));
            }
            case "month" -> {
                LocalDate firstDay = today.withDayOfMonth(1);
                start = toEpochSeconds(firstDay.atStartOfDay());
                end = toEpochSeconds(today.atTime(23, 59, 59));
            }
            case "year" -> {
                LocalDate firstDay = today.withDayOfYear(1);
                start = toEpochSeconds(firstDay.atStartOfDay());
                end = toEpochSeconds(today.atTime(23, 59, 59));
            }
            default -> {
                long[] range = parseCustomTimeRange(time);
                start = range[0];
                end = range[1];
            }
        }

        if (start > 0) {
            wrapper.ge("add_time", start);
        }
        if (end > 0) {
            wrapper.le("add_time", end);
        }
    }

    private long[] parseCustomTimeRange(String timeRange) {
        long[] range = new long[]{0L, 0L};
        if (timeRange == null || timeRange.isEmpty()) {
            return range;
        }

        String normalized = timeRange.replace(" to ", "-");
        String[] parts = normalized.split("\\s*-\\s*");
        if (parts.length < 2) {
            return range;
        }

        long start = parseDateTime(parts[0].trim(), false);
        long end = parseDateTime(parts[parts.length - 1].trim(), true);
        if (end > 0 && start > end) {
            long tmp = start;
            start = end;
            end = tmp;
        }
        range[0] = start;
        range[1] = end;
        return range;
    }

    private long parseDateTime(String value, boolean endOfDay) {
        if (value == null || value.isEmpty()) {
            return 0L;
        }

        for (DateTimeFormatter formatter : RANGE_DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime dt = LocalDateTime.parse(value, formatter);
                return toEpochSeconds(dt);
            } catch (DateTimeParseException ignored) {
            }
        }

        for (DateTimeFormatter formatter : RANGE_DATE_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(value, formatter);
                LocalDateTime dt = endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
                return toEpochSeconds(dt);
            } catch (DateTimeParseException ignored) {
            }
        }

        log.warn("Unable to parse time range value: {}", value);
        return 0L;
    }

    private long toEpochSeconds(LocalDateTime dateTime) {
        return dateTime.atZone(ZONE_ID).toEpochSecond();
    }

    private Map<String, Object> formatFeedbackRecord(ChatServiceFeedbackEntity entity) {
        Map<String, Object> map = new HashMap<>();
        if (entity == null) {
            return map;
        }

        map.put("id", entity.getId());
        map.put("user_id", entity.getUserId());
        map.put("rela_name", entity.getRelaName() == null ? "" : entity.getRelaName());
        map.put("phone", entity.getPhone() == null ? "" : entity.getPhone());
        map.put("content", entity.getContent() == null ? "" : entity.getContent());
        map.put("make", entity.getMake() == null ? "" : entity.getMake());
        map.put("status", entity.getStatus() == null ? 0 : entity.getStatus());

        Integer addTime = entity.getAddTime();
        if (addTime != null && addTime > 0) {
            String formatted = DATE_TIME_FORMATTER.format(Instant.ofEpochSecond(addTime).atZone(ZONE_ID));
            map.put("add_time", formatted);
        } else {
            map.put("add_time", "");
        }

        return map;
    }
}
