package io.renren.crmchat.service;

import io.renren.crmchat.dao.ChatUserLabelAssistMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.service.common.AbstractUserLabelService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserLabelService extends AbstractUserLabelService {

    public AdminUserLabelService(ChatUserLabelMapper chatUserLabelMapper,
                                 ChatUserLabelAssistMapper chatUserLabelAssistMapper) {
        super(chatUserLabelMapper, chatUserLabelAssistMapper);
    }

    public List<ChatUserLabelEntity> getLabelList(Map<String, Object> filters, String appid) {
        return super.listLabels(filters, appid);
    }

    public Map<String, Object> getCreateForm() {
        Map<String, Object> result = new HashMap<>();
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    public Integer saveLabel(Map<String, Object> data, String appid) {
        return super.createLabel(data, appid);
    }

    public boolean moveSort(List<Integer> ids, Integer page, String appid) {
        super.moveLabels(ids, page, appid);
        return true;
    }

    public Map<String, Object> getEditForm(Integer id, String appid) {
        ChatUserLabelEntity label = super.requireOwnedLabel(id, appid);
        Map<String, Object> result = new HashMap<>();
        result.put("label", label);
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    public void updateLabel(Integer id, Map<String, Object> data, String appid) {
        super.updateLabel(id, data, appid);
    }

    public void deleteLabel(Integer id, String appid) {
        super.deleteLabel(id, appid);
    }
}
