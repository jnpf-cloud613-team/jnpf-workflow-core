package jnpf.workflow.activiti.cmd;

import cn.hutool.core.collection.CollectionUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.HistoricActivityInstanceQueryImpl;
import org.activiti.engine.impl.HistoricTaskInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.*;
import org.activiti.engine.runtime.Execution;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 跳转命令类
 *
 * @author yanghuixing
 * @author YMPaaS Cloud@YinMai Info. Co., Ltd
 * @version 0.2.0
 * @since 2023/8/22 17:56
 */
public class JumpCmd implements Command<Void> {
    private final String processInstanceId;

    private List<String> sourceTaskDefIdList;
    private List<String> targetFlowNodeIdList;

    private String deleteReason;

    private final BpmnModel bpmnModel;
    private final RuntimeService runtimeService;

    /**
     * 保存撤回节点的变量map
     */
    private Map<String, List<VariableInstanceEntity>> varMap = new ConcurrentHashMap<>();

    public JumpCmd(String processInstanceId, List<String> sourceTaskDefIdList, List<String> targetFlowNodeIdList,
                   String deleteReason, BpmnModel bpmnModel, RuntimeService runtimeService) {
        this.processInstanceId = processInstanceId;
        this.sourceTaskDefIdList = sourceTaskDefIdList;
        this.deleteReason = deleteReason;
        this.targetFlowNodeIdList = targetFlowNodeIdList;
        this.bpmnModel = bpmnModel;
        this.runtimeService = runtimeService;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        // 处理act_ru_execution
        handleExecution(commandContext);
        // 处理act_hi_actinst
        handleActInst(commandContext);

        targetFlowNodeIdList.forEach(targetId -> {
            UserTask userTask = (UserTask) bpmnModel.getFlowElement(targetId);
            // 创建子执行流，开启任务
            ExecutionEntity processExecution = executionEntityManager.findById(processInstanceId);
            ExecutionEntity childExecution = executionEntityManager.createChildExecution(processExecution);
            childExecution.setCurrentFlowElement(userTask);

            // 设置执行变量
            VariableInstanceEntityManager variableManage = commandContext.getVariableInstanceEntityManager();
            List<VariableInstanceEntity> variableInstanceEntities = varMap.get(userTask.getId());
            if (CollectionUtil.isNotEmpty(variableInstanceEntities)) {
                variableInstanceEntities.forEach(var -> {
                    var.setExecutionId(childExecution.getId());
                    variableManage.insert(var);
                });
            }
            executionEntityManager.insert(childExecution);
            // 交给引擎流转
            commandContext.getAgenda().planContinueProcessOperation(childExecution);
        });
        return null;
    }

    private void handleActInst(CommandContext commandContext) {
        for (String str : sourceTaskDefIdList) {
            HistoricActivityInstanceQueryImpl query =
                    new HistoricActivityInstanceQueryImpl().activityId(str).processInstanceId(processInstanceId).unfinished();
            List<HistoricActivityInstance> activityInstances = commandContext.getHistoricActivityInstanceEntityManager()
                    .findHistoricActivityInstancesByQueryCriteria(query, new Page(0, Integer.MAX_VALUE));
            for (HistoricActivityInstance activity : activityInstances) {
                HistoricActivityInstanceEntity activityEntity = (HistoricActivityInstanceEntity) activity;
                // 修改act_hi_actinst表
                activityEntity.setDeleted(true);
                activityEntity.setDeleteReason(deleteReason);
                commandContext.getHistoricActivityInstanceEntityManager().update(activityEntity);
            }
        }
    }

    private void handleExecution(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        HistoricTaskInstanceEntityManager historicTaskManager = commandContext.getHistoricTaskInstanceEntityManager();
        VariableInstanceEntityManager variableManager = commandContext.getVariableInstanceEntityManager();
        for (String str : sourceTaskDefIdList) {
            List<Execution> executionEntities = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).activityId(str).list();
            for (Execution parentExecution : executionEntities) {
                //关闭未完成的任务执行流
                // 获取子级Executions，如子流程节点等需要处理
                List<ExecutionEntity> childExecutions =
                        executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
                for (ExecutionEntity childExecution : childExecutions) {
                    //因为外键约束,首先要删除variable表中的execution相关数据
                    List<VariableInstanceEntity> variableInstances = variableManager.findVariableInstancesByExecutionId(childExecution.getId());
                    varMap.put(parentExecution.getActivityId(), variableInstances);
                    variableInstances.forEach(variableManager::delete);
                    executionEntityManager.deleteExecutionAndRelatedData(childExecution, deleteReason, false);
                    // 修改历史实例
                    HistoricTaskInstanceQueryImpl query = new HistoricTaskInstanceQueryImpl().executionId(childExecution.getId()).processInstanceId(processInstanceId);
                    List<HistoricTaskInstance> HistoricTaskInstances = historicTaskManager.findHistoricTaskInstancesByQueryCriteria(query);
                    if (CollectionUtil.isNotEmpty(HistoricTaskInstances)) {
                        for (HistoricTaskInstance HistoricTaskInstance : HistoricTaskInstances) {
                            HistoricTaskInstanceEntity entity = (HistoricTaskInstanceEntity) HistoricTaskInstance;
                            entity.setDeleteReason(deleteReason);
                            commandContext.getHistoricTaskInstanceEntityManager().update(entity);
                        }
                    }
                }
                //父执行流关闭
                List<VariableInstanceEntity> variableInstances = variableManager.findVariableInstancesByExecutionId(parentExecution.getId());
                varMap.put(parentExecution.getActivityId(), variableInstances);
                variableInstances.forEach(variableManager::delete);
                ExecutionEntity parentExecution1 = (ExecutionEntity) parentExecution;
                executionEntityManager.deleteExecutionAndRelatedData(parentExecution1, deleteReason, false);
            }
        }
    }
}
