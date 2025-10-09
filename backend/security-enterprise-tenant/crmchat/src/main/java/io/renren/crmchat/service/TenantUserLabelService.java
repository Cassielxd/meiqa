package io.renren.crmchat.service;

import io.renren.crmchat.dao.ChatUserLabelAssistMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.service.common.AbstractUserLabelService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TenantUserLabelService extends AbstractUserLabelService {

    public TenantUserLabelService(ChatUserLabelMapper chatUserLabelMapper,
                                  @Nullable ChatUserLabelAssistMapper chatUserLabelAssistMapper) {
        super(chatUserLabelMapper, chatUserLabelAssistMapper);
    }

    public List<ChatUserLabelEntity> getLabelList(Map<String, Object> filters, String appid) {
        return super.listLabels(filters, appid);
    }

    public Integer createLabel(Map<String, Object> data, String appid) {
        return super.createLabel(data, appid);
    }

    public void updateLabel(Integer id, Map<String, Object> data, String appid) {
        super.updateLabel(id, data, appid);
    }

    public void deleteLabel(Integer id, String appid) {
        super.deleteLabel(id, appid);
    }

    public void moveLabel(List<Integer> ids, Integer page, String appid) {
        super.moveLabels(ids, page, appid);
    }
}
