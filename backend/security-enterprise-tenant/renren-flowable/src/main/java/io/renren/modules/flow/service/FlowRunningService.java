/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.flow.service;

import io.renren.common.page.PageData;
import io.renren.common.utils.DateUtils;
import io.renren.modules.flow.dao.FlowableUserDao;
import io.renren.modules.flow.dto.ProcessInstanceDTO;
import io.renren.modules.flow.user.LoginUser;
import io.renren.modules.flow.utils.PageUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行中的流程
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
@AllArgsConstructor
public class FlowRunningService {
    private final RuntimeService runtimeService;
    private final IdentityService identityService;
    private final FlowableUserDao flowableUserDao;

    /**
     * 流程定义列表
     */
    public PageData<Map<String, Object>> page(Map<String, Object> params) {
        String id = (String)params.get("id");
        String definitionKey = (String)params.get("definitionKey");

        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        if (StringUtils.isNotBlank(id)){
            processInstanceQuery.processInstanceId(id);
        }
        if (StringUtils.isNotBlank(definitionKey)){
            processInstanceQuery.processDefinitionKey(definitionKey);
        }

        List<ProcessInstance> processInstanceList = processInstanceQuery.listPage(PageUtils.getPageOffset(params), PageUtils.getPageLimit(params));
        List<Map<String, Object>> objectList = new ArrayList<>();
        for (ProcessInstance processInstance : processInstanceList) {
            objectList.add(processInstanceConvert(processInstance));
        }
        return new PageData<>(objectList, (int)processInstanceQuery.count());
    }

    /**
     * 流程实例信息
     */
    private Map<String, Object> processInstanceConvert(ProcessInstance processInstance) {
        ExecutionEntityImpl execution = (ExecutionEntityImpl) processInstance;

        Map<String, Object> map = new HashMap<>(9);
        map.put("id", execution.getId());
        map.put("processInstanceId", execution.getProcessInstanceId());
        map.put("processDefinitionId", execution.getProcessDefinitionId());
        map.put("processDefinitionName", execution.getProcessDefinitionName());
        map.put("processDefinitionKey", execution.getProcessDefinitionKey());
        map.put("businessKey", execution.getBusinessKey());
        map.put("activityId", execution.getActivityId());
        map.put("activityName", execution.getActivityName());
        map.put("suspended", execution.isSuspended());
        map.put("startTime", DateUtils.format(execution.getStartTime(), DateUtils.DATE_TIME_PATTERN));

        String userName = flowableUserDao.getUserName(execution.getStartUserId());
        if (StringUtils.isNotBlank(userName)) {
            map.put("startUserName", userName);
        }

        return map;
    }

    /**
     * 删除实例
     * @param id  实例ID
     */
    public void delete(String id){
        runtimeService.deleteProcessInstance(id, null);
    }

    /**
     * 启动流程实例
     * @param processDefinitionId 流程定义id
     * @param businessKey         业务key
     * @param variables           流程参数
     */
    public ProcessInstanceDTO startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
        //启动流程用户
        identityService.setAuthenticatedUserId(LoginUser.getUserId().toString());

        //启动流程
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, businessKey, variables);

        ProcessInstanceDTO process = new ProcessInstanceDTO();
        process.setProcessInstanceId(processInstance.getId());
        return process;
    }
}
