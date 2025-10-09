package io.renren.modules.flow.common.behavior;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import io.renren.common.utils.SpringContextUtils;
import io.renren.modules.flow.common.ProcessConstants;
import io.renren.modules.flow.dao.FlowableUserDao;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.List;

public class RenUserTaskActivityBehavior extends UserTaskActivityBehavior {
    public RenUserTaskActivityBehavior(UserTask userTask) {
        super(userTask);
    }

    @Override
    protected void handleAssignments(TaskService taskService, String assignee, String owner, List<String> candidateUsers,
                                     List<String> candidateGroups, TaskEntity task, ExpressionManager expressionManager,
                                     DelegateExecution execution, ProcessEngineConfigurationImpl processEngineConfiguration) {
        // 审核人员类型
        String dataType = execution.getCurrentFlowElement().getAttributeValue(ProcessConstants.NAME_SPASE, ProcessConstants.DATA_TYPE);

        // 如果是发起人、指定人、变量、获选人，则无需处理
        if (StrUtil.equalsAnyIgnoreCase(dataType, ProcessConstants.START_USER_ID, ProcessConstants.ASSIGNEE, ProcessConstants.VARIABLE, ProcessConstants.CANDIDATE_USERS)) {
            // 无需处理
            super.handleAssignments(taskService, assignee, owner, candidateUsers, candidateGroups, task, expressionManager, execution, processEngineConfiguration);

            return;
        }

        FlowableUserDao flowableUserDao = (FlowableUserDao) SpringContextUtils.getBean("flowableUserDao");
        List<Long> candidateGroupList = candidateGroups.stream().map(Long::parseLong).toList();

        // 如果是部门领导
        if (StrUtil.equalsIgnoreCase(dataType, ProcessConstants.DEPT_LEADER)) {
            // 启动用户ID
            String startUserId = execution.getVariable(ProcessConstants.START_USER_ID).toString();
            // 查询部门领导的用户ID
            Long userId = flowableUserDao.getLeaderIdListByUserId(Long.parseLong(startUserId));
            if (userId != null) {
                assignee = userId.toString();
            }

            super.handleAssignments(taskService, assignee, owner, candidateUsers, candidateGroups, task, expressionManager, execution, processEngineConfiguration);

            return;
        }

        // 如果是候选角色
        if (StrUtil.equalsIgnoreCase(dataType, ProcessConstants.CANDIDATE_ROLE)) {
            // 查询用户ID列表
            List<Long> userIdList = flowableUserDao.getUserIdListByRoleIdList(candidateGroupList);
            candidateUsers = userIdList.stream().map(String::valueOf).toList();

            super.handleAssignments(taskService, assignee, owner, candidateUsers, ListUtil.empty(), task, expressionManager, execution, processEngineConfiguration);

            return;
        }

        // 如果是候选岗位
        if (StrUtil.equalsIgnoreCase(dataType, ProcessConstants.CANDIDATE_POST)) {
            // 查询用户ID列表
            List<Long> userIdList = flowableUserDao.getUserIdListByPostIdList(candidateGroupList);
            candidateUsers = userIdList.stream().map(String::valueOf).toList();

            super.handleAssignments(taskService, assignee, owner, candidateUsers, ListUtil.empty(), task, expressionManager, execution, processEngineConfiguration);

            return;
        }

        // 如果是候选部门
        if (StrUtil.equalsIgnoreCase(dataType, ProcessConstants.CANDIDATE_DEPT)) {
            // 查询用户ID列表
            List<Long> userIdList = flowableUserDao.getLeaderIdListByDeptIdList(candidateGroupList);
            candidateUsers = userIdList.stream().map(String::valueOf).toList();

            super.handleAssignments(taskService, assignee, owner, candidateUsers, ListUtil.empty(), task, expressionManager, execution, processEngineConfiguration);

            return;
        }


        super.handleAssignments(taskService, assignee, owner, candidateUsers, candidateGroups, task, expressionManager, execution, processEngineConfiguration);
    }
}
