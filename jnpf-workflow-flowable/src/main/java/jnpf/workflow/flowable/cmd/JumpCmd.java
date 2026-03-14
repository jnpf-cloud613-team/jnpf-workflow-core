package jnpf.workflow.flowable.cmd;

import cn.hutool.core.collection.CollectionUtil;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.HistoricActivityInstanceQueryImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.Execution;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.HistoricTaskService;
import org.flowable.task.service.impl.HistoricTaskInstanceQueryImpl;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.flowable.variable.service.VariableService;
import org.flowable.variable.service.impl.persistence.entity.VariableInstanceEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 跳转命令类
 * 参考：https://blog.csdn.net/zhsp419/article/details/114264451
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/9 17:45
 */
public class JumpCmd implements Command<Void> {
    private final String processInstanceId;

    private final List<String> sourceTaskDefIdList;
    private final List<String> targetFlowNodeIdList;

    private final String deleteReason;

    private final BpmnModel bpmnModel;
    private final RuntimeService runtimeService;

    /**
     * 保存撤回节点的变量map
     */
    private final Map<String, List<VariableInstanceEntity>> varMap = new ConcurrentHashMap<>();

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
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
        // 处理act_ru_execution
        handleExecution(commandContext);
        // 处理act_hi_actinst
        handleActInst(commandContext);

        targetFlowNodeIdList.forEach(targetId -> {
            FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(targetId);
            // 创建子执行流，开启任务
            ExecutionEntity processExecution = executionEntityManager.findById(processInstanceId);
            ExecutionEntity childExecution = executionEntityManager.createChildExecution(processExecution);
            childExecution.setCurrentFlowElement(flowNode);

            // 设置执行变量
            VariableService variableService = CommandContextUtil.getVariableService();
            List<VariableInstanceEntity> variableInstanceEntities = varMap.get(flowNode.getId());
            if (CollectionUtil.isNotEmpty(variableInstanceEntities)) {
                variableInstanceEntities.forEach(var -> {
                    var.setExecutionId(childExecution.getId());
                    variableService.insertVariableInstance(var);
                });
            }
            executionEntityManager.insert(childExecution);
            // 交给引擎流转
            CommandContextUtil.getAgenda().planContinueProcessOperation(childExecution);
        });
        return null;
    }

    private void handleActInst(CommandContext commandContext) {
        for (String str : sourceTaskDefIdList) {
            HistoricActivityInstanceQueryImpl query = new HistoricActivityInstanceQueryImpl()
                    .activityId(str).processInstanceId(processInstanceId).unfinished();
            List<HistoricActivityInstance> activityInstances = CommandContextUtil.getHistoricActivityInstanceEntityManager()
                    .findHistoricActivityInstancesByQueryCriteria(query);
            for (HistoricActivityInstance activity : activityInstances) {
                HistoricActivityInstanceEntity activityEntity = (HistoricActivityInstanceEntity) activity;
                // 修改act_hi_actinst表
                activityEntity.setDeleted(true);
                activityEntity.setDeleteReason(deleteReason);
                CommandContextUtil.getHistoricActivityInstanceEntityManager().update(activityEntity);
            }
        }
    }

    private void handleExecution(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
        HistoricTaskService historicTaskService = CommandContextUtil.getHistoricTaskService();
        VariableService variableService = CommandContextUtil.getVariableService();
        for (String str : sourceTaskDefIdList) {
            List<Execution> executionEntities = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).activityId(str).list();
            for (Execution parentExecution : executionEntities) {
                //关闭未完成的任务执行流
                // 获取子级Executions，如子流程节点等需要处理
                List<ExecutionEntity> childExecutions =
                        executionEntityManager.findChildExecutionsByParentExecutionId(parentExecution.getId());
                for (ExecutionEntity childExecution : childExecutions) {
                    //因为外键约束,首先要删除variable表中的execution相关数据
                    List<VariableInstanceEntity> variableInstances = variableService.findVariableInstancesByExecutionId(childExecution.getId());
                    varMap.put(parentExecution.getActivityId(), variableInstances);
                    variableInstances.forEach(variableService::deleteVariableInstance);
                    executionEntityManager.deleteExecutionAndRelatedData(childExecution, deleteReason, false);
                    // 修改历史实例
                    HistoricTaskInstanceQueryImpl query = new HistoricTaskInstanceQueryImpl().executionId(childExecution.getId()).processInstanceId(processInstanceId);
                    List<HistoricTaskInstance> HistoricTaskInstances = historicTaskService.findHistoricTaskInstancesByQueryCriteria(query);
                    if (CollectionUtil.isNotEmpty(HistoricTaskInstances)) {
                        for (HistoricTaskInstance HistoricTaskInstance : HistoricTaskInstances) {
                            HistoricTaskInstanceEntity entity = (HistoricTaskInstanceEntity) HistoricTaskInstance;
                            entity.setDeleteReason(deleteReason);
                            historicTaskService.updateHistoricTask(entity, true);
                        }
                    }
                }
                //父执行流关闭
                List<VariableInstanceEntity> variableInstances = variableService.findVariableInstancesByExecutionId(parentExecution.getId());
                varMap.put(parentExecution.getActivityId(), variableInstances);
                variableInstances.forEach(variableService::deleteVariableInstance);
                ExecutionEntity parentExecution1 = (ExecutionEntity) parentExecution;
                executionEntityManager.deleteExecutionAndRelatedData(parentExecution1, deleteReason, false);
            }
        }
    }
}
