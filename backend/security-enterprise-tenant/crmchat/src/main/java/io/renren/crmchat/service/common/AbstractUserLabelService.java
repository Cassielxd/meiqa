package io.renren.crmchat.service.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserLabelAssistMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.entity.ChatUserLabelAssistEntity;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.security.TenantContextUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shared domain logic for user label management.
 */
public abstract class AbstractUserLabelService {

    private final ChatUserLabelMapper chatUserLabelMapper;
    private final ChatUserLabelAssistMapper chatUserLabelAssistMapper;

    protected AbstractUserLabelService(ChatUserLabelMapper chatUserLabelMapper,
                                       ChatUserLabelAssistMapper chatUserLabelAssistMapper) {
        this.chatUserLabelMapper = chatUserLabelMapper;
        this.chatUserLabelAssistMapper = chatUserLabelAssistMapper;
    }

    public List<ChatUserLabelEntity> listLabels(Map<String, Object> filters, String appid) {
        QueryWrapper<ChatUserLabelEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", getDefaultUserId());

        Object cateId = filters != null ? filters.get("cate_id") : null;
        if (cateId != null && !Objects.toString(cateId, "").trim().isEmpty()) {
            wrapper.eq("cate_id", cateId);
        }

        wrapper.orderByAsc("sort", "id");
        return chatUserLabelMapper.selectList(wrapper);
    }

    public Integer createLabel(Map<String, Object> data, String appid) {
        validateLabelPayload(data);

        ChatUserLabelEntity entity = new ChatUserLabelEntity();
        entity.setAppid(appid);
        entity.setLabel(Objects.toString(data.get("label")));
        entity.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        entity.setSort((Integer) data.getOrDefault("sort", 0));
        entity.setUserId(getDefaultUserId());

        chatUserLabelMapper.insert(entity);
        entity.setSort(entity.getId());
        chatUserLabelMapper.updateById(entity);
        return entity.getId();
    }

    public void updateLabel(Integer id, Map<String, Object> data, String appid) {
        validateLabelPayload(data);

        ChatUserLabelEntity entity = requireOwnedLabel(id, appid);
        entity.setLabel(Objects.toString(data.get("label")));
        entity.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        if (data.containsKey("sort") && data.get("sort") != null) {
            entity.setSort(Integer.parseInt(data.get("sort").toString()));
        }
        chatUserLabelMapper.updateById(entity);
    }

    public void deleteLabel(Integer id, String appid) {
        ChatUserLabelEntity entity = requireOwnedLabel(id, appid);
        preDeleteCheck(id, appid);
        chatUserLabelMapper.deleteById(entity.getId());
    }

    public void moveLabels(List<Integer> ids, Integer page, String appid) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (int i = 0; i < ids.size(); i++) {
            ChatUserLabelEntity entity = chatUserLabelMapper.selectById(ids.get(i));
            if (entity != null) {
                entity.setSort(calculateSortValue(page, i));
                chatUserLabelMapper.updateById(entity);
            }
        }
    }

    protected ChatUserLabelEntity requireOwnedLabel(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }
        ChatUserLabelEntity entity = chatUserLabelMapper.selectById(id);
        if (entity == null) {
            throw new CrmChatException("Label does not exist");
        }
        if (TenantContextUtils.isTenantIsolationEnabled()
                && entity.getAppid() != null
                && !Objects.equals(entity.getAppid(), TenantContextUtils.currentAppid())) {
            throw new CrmChatException("Label does not exist");
        }
        return entity;
    }

    protected void preDeleteCheck(Integer id, String appid) {
        if (chatUserLabelAssistMapper == null) {
            return;
        }
        QueryWrapper<ChatUserLabelAssistEntity> wrapper = new QueryWrapper<ChatUserLabelAssistEntity>().eq("label_id", id);
        if (chatUserLabelAssistMapper.selectCount(wrapper) > 0) {
            throw new CrmChatException("Please remove users associated with this label first");
        }
    }

    protected int calculateSortValue(Integer page, int index) {
        if (page == null || page < 0) {
            return index + 1;
        }
        return page * 100 + index + 1;
    }

    protected int getDefaultUserId() {
        return 0;
    }

    private void validateLabelPayload(Map<String, Object> data) {
        if (data == null || data.get("label") == null || Objects.toString(data.get("label")).trim().isEmpty()) {
            throw new CrmChatException("Label name is required");
        }
        if (data.get("cate_id") == null || Objects.toString(data.get("cate_id")).trim().isEmpty()) {
            throw new CrmChatException("Please select a label category");
        }
    }
}
