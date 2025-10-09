package io.renren.modules.flow.common.handler;

import cn.hutool.core.util.StrUtil;
import io.renren.modules.flow.common.ProcessConstants;
import io.renren.modules.flow.dao.FlowableUserDao;
import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 多实例处理
 */
@Component
@AllArgsConstructor
public class MultiInstanceHandler {
    private final FlowableUserDao flowableUserDao;

    /**
     * 获取多实例，审核人列表
     */
    public Set<String> getList(DelegateExecution execution) {
        // 审核人列表
        Set<String> userIdSet = new LinkedHashSet<>();

        // 获取当前节点
        FlowElement flowElement = execution.getCurrentFlowElement();
        UserTask userTask = (UserTask) flowElement;
        // 审核人员类型
        String dataType = userTask.getAttributeValue(ProcessConstants.NAME_SPASE, ProcessConstants.DATA_TYPE);

        // 如果是候选用户
        if (StrUtil.equalsAnyIgnoreCase(dataType, ProcessConstants.CANDIDATE_USERS)) {
            userIdSet.addAll(userTask.getCandidateUsers());

            return userIdSet;
        }

        // 如果是候选角色
        if (StrUtil.equalsAnyIgnoreCase(dataType, ProcessConstants.CANDIDATE_ROLE)) {
            List<Long> candidateGroupList = userTask.getCandidateGroups().stream().map(Long::parseLong).toList();
            List<Long> userIdList = flowableUserDao.getUserIdListByRoleIdList(candidateGroupList);
            userIdSet.addAll(userIdList.stream().map(String::valueOf).toList());

            return userIdSet;
        }

        // 如果是候选岗位
        if (StrUtil.equalsAnyIgnoreCase(dataType, ProcessConstants.CANDIDATE_POST)) {
            List<Long> candidateGroupList = userTask.getCandidateGroups().stream().map(Long::parseLong).toList();
            List<Long> userIdList = flowableUserDao.getUserIdListByPostIdList(candidateGroupList);
            userIdSet.addAll(userIdList.stream().map(String::valueOf).toList());

            return userIdSet;
        }

        // 如果是候选部门
        if (StrUtil.equalsAnyIgnoreCase(dataType, ProcessConstants.CANDIDATE_DEPT)) {
            List<Long> candidateGroupList = userTask.getCandidateGroups().stream().map(Long::parseLong).toList();
            List<Long> userIdList = flowableUserDao.getLeaderIdListByDeptIdList(candidateGroupList);
            userIdSet.addAll(userIdList.stream().map(String::valueOf).toList());

            return userIdSet;
        }

        return userIdSet;
    }
}
