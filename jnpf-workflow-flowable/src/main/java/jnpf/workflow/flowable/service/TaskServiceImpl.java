package jnpf.workflow.flowable.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import jnpf.workflow.common.exception.BizException;
import jnpf.workflow.common.exception.ResultCode;
import jnpf.workflow.common.model.fo.*;
import jnpf.workflow.common.model.vo.*;
import jnpf.workflow.common.service.IInstanceService;
import jnpf.workflow.common.service.ITaskService;
import jnpf.workflow.flowable.cmd.JumpCmd;
import jnpf.workflow.flowable.util.FlowableUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程任务实现层
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 11:12
 */
@Slf4j
@Service
@AllArgsConstructor
public class TaskServiceImpl implements ITaskService {
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final ManagementService managementService;
    private final IInstanceService instanceService;

    @Override
    public List<TaskVo> getTask(String instanceId) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(instanceId).list();
        List<TaskVo> vos = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(list)) {
            for (Task task : list) {
                TaskVo vo = new TaskVo();
                vo.setTaskId(task.getId());
                vo.setTaskName(task.getName());
                vo.setTaskKey(task.getTaskDefinitionKey());
                vo.setInstanceId(task.getProcessInstanceId());
                vos.add(vo);
            }
        }
        return vos;
    }

    @Override
    public boolean complete(TaskCompleteFo fo) {
        Task task = taskService.createTaskQuery().taskId(fo.getTaskId()).singleResult();
        if (null == task) {
            throw new BizException(ResultCode.TASK_NOT_EXIST);
        }
        try {
            if (CollectionUtil.isNotEmpty(fo.getVariables())) {
                taskService.complete(fo.getTaskId(), fo.getVariables());
            } else {
                taskService.complete(fo.getTaskId());
            }
            return true;
        } catch (Exception e) {
            log.error(ResultCode.TASK_COMPLETE_ERROR.getMsg(), e);
        }
        return false;
    }

    @Override
    public boolean moveSingleToMulti(MoveSingleToMultiFo fo) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(fo.getInstanceId()).singleResult();
        if (null == instance) {
            throw new BizException(ResultCode.INSTANCE_NOT_EXIST);
        }
        try {
            this.moveSingleActivityIdToActivityIds(fo.getInstanceId(), fo.getSourceKey(), fo.getTargetKeys());
            return true;
        } catch (Exception e) {
            log.error(ResultCode.TASK_JUMP_ERROR.getMsg(), e);
        }
        return false;
    }

    /**
     * 节点跳转
     * Set the activity id that should be changed to multiple activity ids
     */
    public void moveSingleActivityIdToActivityIds(String processInstanceId, String activityId, List<String> activityIds) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveSingleActivityIdToActivityIds(activityId, activityIds).changeState();
    }

    @Override
    public boolean moveMultiToSingle(MoveMultiToSingleFo fo) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(fo.getInstanceId()).singleResult();
        if (null == instance) {
            throw new BizException(ResultCode.INSTANCE_NOT_EXIST);
        }
        try {
            this.moveActivityIdsToSingleActivityId(fo.getInstanceId(), fo.getSourceKeys(), fo.getTargetKey());
            return true;
        } catch (Exception e) {
            log.error(ResultCode.TASK_JUMP_ERROR.getMsg(), e);
        }
        return false;
    }

    /**
     * 节点跳转
     * Set the activity ids that should be changed to a single activity id
     */
    public void moveActivityIdsToSingleActivityId(String processInstanceId, List<String> activityIds, String activityId) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveActivityIdsToSingleActivityId(activityIds, activityId).changeState();
    }

    @Override
    public boolean jump(JumpFo fo) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(fo.getInstanceId()).singleResult();
        if (null == instance) {
            throw new BizException(ResultCode.INSTANCE_NOT_EXIST);
        }
        try {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
            JumpCmd jumpCmd = new JumpCmd(fo.getInstanceId(), fo.getSource(), fo.getTarget(), "custom jump", bpmnModel, runtimeService);
            managementService.executeCommand(jumpCmd);
            return true;
        } catch (Exception e) {
            log.error(ResultCode.TASK_JUMP_ERROR.getMsg(), e);
        }
        return false;
    }

    @Override
    public List<String> getFallbacks(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (null == task) {
            throw new BizException(ResultCode.TASK_NOT_EXIST);
        }
        FlowElement source = getFlowElement(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        List<String> list = FlowableUtil.getPassActs(source, null, null);
        return list.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 根据流程定义ID和任务KEY 获取节点元素
     */
    public FlowElement getFlowElement(String processDefinitionId, String taskDefinitionKey) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (null == definition) {
            throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
        }
        Process process = repositoryService.getBpmnModel(definition.getId()).getProcesses().get(0);
        Collection<FlowElement> elements = FlowableUtil.getAllElements(process.getFlowElements(), null);
        FlowElement source = null;
        if (null != elements && !elements.isEmpty()) {
            for (FlowElement element : elements) {
                if (element.getId().equals(taskDefinitionKey)) {
                    source = element;
                }
            }
        }
        return source;
    }

    @Override
    public List<String> back(TaskBackFo fo) {
        Task task = taskService.createTaskQuery().taskId(fo.getTaskId()).singleResult();
        String definitionId;
        String instanceId;
        if (null == task) {
            HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(fo.getTaskId()).singleResult();
            definitionId = historicTask.getProcessDefinitionId();
            instanceId = historicTask.getProcessInstanceId();
        } else {
            definitionId = task.getProcessDefinitionId();
            instanceId = task.getProcessInstanceId();
        }
        if (StrUtil.isNotBlank(fo.getTargetKey())) {
            String[] split = fo.getTargetKey().split(",");
            if (split.length == 0) {
                throw new BizException("目标节点编码不能为空");
            }
            List<String> list = Arrays.asList(split);
            return this.back(definitionId, instanceId, list);
        }
        return null;
    }

    public List<String> back(String definitionId, String instanceId,  List<String> targetList) {
        List<String> currentIds = new ArrayList<>();
        for (String targetKey : targetList) {
            FlowElement target = getFlowElement(definitionId, targetKey);
            // 获取所有正常进行的任务节点Key，用于找出需要撤回的任务
            List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(instanceId).list();
            List<String> runTaskKeyList = runTaskList.stream().map(Task::getTaskDefinitionKey).collect(Collectors.toList());
            // 需驳回的任务列表
            List<UserTask> userTaskList = FlowableUtil.getChildUserTasks(target, runTaskKeyList, null, null);
            List<String> collect = userTaskList.stream().map(UserTask::getId).collect(Collectors.toList());
            currentIds.addAll(collect);
        }
        currentIds = currentIds.stream().distinct().collect(Collectors.toList());

        JumpFo jumpFo = new JumpFo();
        jumpFo.setInstanceId(instanceId);
        jumpFo.setSource(currentIds);
        jumpFo.setTarget(targetList);
        this.jump(jumpFo);

        return currentIds;
    }

    @Override
    public List<String> getPrevUserTask(TaskPrevFo fo) {
        List<String> list = new ArrayList<>();
        if (StrUtil.isNotBlank(fo.getDeploymentId())) {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(fo.getDeploymentId()).singleResult();
            if (null == definition) {
                throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
            }
            // 获取当前节点
            FlowElement source = getFlowElement(definition.getId(), fo.getTaskKey());
            // 获取下一级用户任务
            list = FlowableUtil.getParentActs(source, null, null);
        } else {
            Task task = taskService.createTaskQuery().taskId(fo.getTaskId()).singleResult();
            if (null == task) {
                throw new BizException(ResultCode.TASK_NOT_EXIST);
            }
            FlowElement source = getFlowElement(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            list = FlowableUtil.getParentActs(source, null, null);
        }
        return list;
    }

    @Override
    public List<NodeElementVo> getNextUserTask(TaskNextFo fo) {
        List<UserTask> nextUserTasks = new ArrayList<>();
        if (StrUtil.isNotBlank(fo.getDeploymentId())) {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(fo.getDeploymentId()).singleResult();
            if (null == definition) {
                throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
            }
            // 获取当前节点
            FlowElement source = getFlowElement(definition.getId(), fo.getTaskKey());
            // 获取下一级用户任务
            nextUserTasks = FlowableUtil.getNextUserTasks(source, null, null);
        } else {
            HistoricTaskInstance taskInst = historyService.createHistoricTaskInstanceQuery().taskId(fo.getTaskId()).singleResult();
            if (null == taskInst) {
                throw new BizException(ResultCode.TASK_NOT_EXIST);
            }
            FlowElement source = getFlowElement(taskInst.getProcessDefinitionId(), taskInst.getTaskDefinitionKey());
            // 获取下一级用户任务
            nextUserTasks = FlowableUtil.getNextUserTasks(source, null, null);
        }
        List<NodeElementVo> vos = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(nextUserTasks)) {
            for (UserTask task : nextUserTasks) {
                NodeElementVo vo = new NodeElementVo();
                vo.setId(task.getId());
                vo.setName(task.getName());
                vo.setIncomingList(task.getIncomingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                vo.setOutgoingList(task.getOutgoingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                vos.add(vo);
            }
        }
        return vos;
    }

    @Override
    public List<String> getTaskKeyAfterFlow(FlowTargetTaskFo fo) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(fo.getDeploymentId()).singleResult();
        if (null == definition) {
            throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
        }
        List<String> list = new ArrayList<>();
        // 获取当前节点
        FlowElement source = getFlowElement(definition.getId(), fo.getFlowKey());
        String taskKey = FlowableUtil.getTaskKeyAfterFlow(source);
        list.add(taskKey);
        return list;
    }

    @Override
    public boolean retract(String taskId) {
        // 需要撤回的任务实例
        HistoricTaskInstance taskInst = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (null != taskInst) {
            ProcessInstance procInst = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(taskInst.getProcessInstanceId())
                    .active().singleResult();
            if (null != procInst) {
                // 获取当前节点
                FlowElement source = getFlowElement(taskInst.getProcessDefinitionId(), taskInst.getTaskDefinitionKey());
                // 获取下一级用户任务
                List<UserTask> nextUserTasks = FlowableUtil.getNextUserTasks(source, null, null);
                List<String> nextUserTaskKeys = nextUserTasks.stream().map(UserTask::getId).collect(Collectors.toList());
                // 获取所有运行的任务节点，找到需要撤回的任务
                List<Task> activateTasks = taskService.createTaskQuery().processInstanceId(taskInst.getProcessInstanceId()).list();
                List<String> currentIds = new ArrayList<>();
                for (Task task : activateTasks) {
                    // 检查激活的任务节点是否存在下一级中，如果存在，则加入到需要撤回的节点
                    if (CollUtil.contains(nextUserTaskKeys, task.getTaskDefinitionKey())) {
                        currentIds.add(task.getTaskDefinitionKey());
                    }
                }
                this.moveActivityIdsToSingleActivityId(taskInst.getProcessInstanceId(), currentIds, taskInst.getTaskDefinitionKey());
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getOutgoingFlows(TaskOutgoingFo fo) {
        if (StrUtil.isNotBlank(fo.getDeploymentId())) {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(fo.getDeploymentId()).singleResult();
            if (null == definition) {
                throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
            }
            return this.getOutgoingFlows(definition.getId(), fo.getTaskKey());
        } else {
            Task task = taskService.createTaskQuery().taskId(fo.getTaskId()).singleResult();
            if (null == task) {
                throw new BizException(ResultCode.TASK_NOT_EXIST);
            }
            return this.getOutgoingFlows(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        }
    }

    /**
     * 获取出线Key集合（若出线的出口为网关，则一并获取网关的出线）
     */
    public List<String> getOutgoingFlows(String processDefinitionId, String taskDefinitionKey) {
        FlowElement source = getFlowElement(processDefinitionId, taskDefinitionKey);
        List<SequenceFlow> flows = new ArrayList<>();
        flows = FlowableUtil.getOutFlowsWithGateway(source, flows);
        List<String> list = new ArrayList<>();
        if (!flows.isEmpty()) {
            for (SequenceFlow flow : flows) {
                list.add(flow.getId());
            }
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<FlowVo> getOutgoing(TaskOutgoingFo fo) {
        if (StrUtil.isNotBlank(fo.getDeploymentId())) {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(fo.getDeploymentId()).singleResult();
            if (null == definition) {
                throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
            }
            return this.getOutgoing(definition.getId(), fo.getTaskKey());
        } else {
            Task task = taskService.createTaskQuery().taskId(fo.getTaskId()).singleResult();
            if (null == task) {
                throw new BizException(ResultCode.TASK_NOT_EXIST);
            }
            return this.getOutgoing(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        }
    }

    public List<FlowVo> getOutgoing(String processDefinitionId, String taskDefinitionKey) {
        FlowElement source = getFlowElement(processDefinitionId, taskDefinitionKey);
        return FlowableUtil.getOutFlows(source, null);
    }

    @Override
    public List<String> getKeysOfFinished(String instanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (null == instance) {
            throw new BizException(ResultCode.INSTANCE_NOT_EXIST);
        }
        // 获取当前节点之前的节点
        List<String> keysOfBefore = getKeysOfBefore(instance);
        // 获取流程实例下完成的历史活动
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId).finished().list();
        if (CollectionUtil.isNotEmpty(list)) {
            // 去除线，并去重
            List<String> keysOfFinished = list.stream()
                    .filter(e -> !BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW.equals(e.getActivityType()))
                    .sorted(Comparator.comparing(HistoricActivityInstance::getStartTime))
                    .map(HistoricActivityInstance::getActivityId)
                    .distinct().collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(keysOfBefore)) {
                keysOfFinished.retainAll(keysOfBefore);
            }
            return keysOfFinished;
        }
        return null;
    }

    /**
     * 遍历当前节点，获取之前的节点
     */
    public List<String> getKeysOfBefore(ProcessInstance instance) {
        List<String> res = new ArrayList<>();
        // 获取实例下的当前任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
        if (CollectionUtil.isNotEmpty(taskList)) {
            List<String> keys = taskList.stream().map(TaskInfo::getTaskDefinitionKey).collect(Collectors.toList());
            for (String key : keys) {
                FlowElement source = getFlowElement(instance.getProcessDefinitionId(), key);
                List<String> list = FlowableUtil.getBefore(source, null, null);
                res.addAll(list);
            }
        }
        return res.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getIncomingFlows(String taskId) {
        HistoricTaskInstance taskInst = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (null != taskInst) {
            FlowElement source = getFlowElement(taskInst.getProcessDefinitionId(), taskInst.getTaskDefinitionKey());
            List<SequenceFlow> flows = FlowableUtil.getElementIncomingFlows(source);
            return flows.stream().map(BaseElement::getId).collect(Collectors.toList());
        }
        return null;
    }

    // 获取未经过的节点
    @Override
    public List<String> getToBePass(String instanceId) {
        List<String> list = new ArrayList<>();
        List<Task> currentList = taskService.createTaskQuery().processInstanceId(instanceId).list();
        if (CollectionUtil.isNotEmpty(currentList)) {
            List<String> collect = currentList.stream().map(Task::getTaskDefinitionKey).collect(Collectors.toList());
            // 根据当前的节点 递归后续的所有节点
            for (Task task : currentList) {
                FlowElement source = getFlowElement(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
                List<String> after = FlowableUtil.getAfter(source, null, null);
                list.addAll(after);
            }
            list = list.stream().filter(e -> !collect.contains(e)).collect(Collectors.toList());
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getAfter(TaskAfterFo fo) {
        String deploymentId = fo.getDeploymentId();
        List<String> taskKeys = fo.getTaskKeys();
        List<String> list = new ArrayList<>();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        if (null == definition) {
            throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
        }
        for (String taskKey : taskKeys) {
            FlowElement source = getFlowElement(definition.getId(), taskKey);
            List<String> after = FlowableUtil.getAfter(source, null, null);
            list.addAll(after);
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

    // complete 异常 补偿
    @Override
    public List<TaskVo> compensate(CompensateFo fo) {
        String instanceId = fo.getInstanceId();
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (null == instance) {
            HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            Map<String, Object> variables = new HashMap<>();
            List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(instanceId).list();
            for (HistoricVariableInstance var : list) {
                variables.put(var.getVariableName(), var.getValue());
            }
            String deploymentId = historicInstance.getDeploymentId();

            InstanceStartFo startFo = new InstanceStartFo();
            startFo.setDeploymentId(deploymentId);
            startFo.setVariables(variables);
            InstanceVo instanceVo = instanceService.startById(startFo);
            instanceId = instanceVo.getInstanceId();
        }
        List<String> sourceList = fo.getSource().stream().sorted().collect(Collectors.toList());

        List<TaskVo> taskVoList = this.getTask(instanceId);
        List<String> currentList = taskVoList.stream().map(TaskVo::getTaskKey).sorted().collect(Collectors.toList());

//        if (ObjectUtil.equals(sourceList, currentList)) {
//            return null == instance ? taskVoList : new ArrayList<>();
//        }

        // 获取需要跳转的节点集合、目标节点集合
        List<String> createList = sourceList.stream().filter(e -> !currentList.contains(e)).collect(Collectors.toList());
        List<String> deleteList = currentList.stream().filter(e -> !sourceList.contains(e)).collect(Collectors.toList());

        JumpFo jumpFo = new JumpFo();
        jumpFo.setInstanceId(instanceId);
        jumpFo.setSource(deleteList);
        jumpFo.setTarget(createList);
        this.jump(jumpFo);

        List<TaskVo> vos = this.getTask(instanceId);
        if (null != instance) {
            vos.forEach(e -> e.setInstanceId(null));
        }
        return vos;
    }

    @Override
    public List<HistoricNodeVo> getHistoric(String instanceId) {
        List<HistoricNodeVo> vos;

        Set<String> set = new HashSet<>();
        set.add("userTask");
        set.add("startEvent");

        vos = this.getHistoricVos(instanceId, set);

        return vos.stream().sorted(Comparator.comparing(HistoricNodeVo::getStartTime)).collect(Collectors.toList());
    }

    // 获取历史结束节点
    @Override
    public List<String> getHistoricEnd(String instanceId) {
        List<String> list = new ArrayList<>();
        List<HistoricNodeVo> vos = this.getHistoricVos(instanceId, null);
        if (CollectionUtil.isNotEmpty(vos)) {
            list = vos.stream().map(HistoricNodeVo::getCode).distinct().collect(Collectors.toList());
        }
        return list;
    }

    public List<HistoricNodeVo> getHistoricVos(String instanceId, Set<String> set) {
        List<HistoricNodeVo> vos = new ArrayList<>();
        if (CollectionUtil.isEmpty(set)) {
            set = new HashSet<>();
            set.add("endEvent");
        }
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .activityTypes(set)
                .processInstanceId(instanceId).list();
        if (CollectionUtil.isNotEmpty(list)) {
            for (HistoricActivityInstance act : list) {
                HistoricNodeVo vo = new HistoricNodeVo();
                vo.setCode(act.getActivityId());
                vo.setTaskId(act.getTaskId());
                vo.setStartTime(act.getStartTime().getTime());
                vos.add(vo);
            }
        }
        return vos;
    }

    @Override
    public NodeElementVo getElementInfo(InfoModel model) {
        String deploymentId = model.getDeploymentId();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        if (null == definition) {
            throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
        }
        String definitionId = definition.getId();
        String key = model.getKey();
        NodeElementVo vo = new NodeElementVo();
        FlowElement source = getFlowElement(definitionId, key);
        if (null != source) {
            vo.setId(source.getId());
            List<SequenceFlow> outgoingFlows = FlowableUtil.getElementOutgoingFlows(source);
            vo.setOutgoingList(outgoingFlows.stream().map(SequenceFlow::getId).collect(Collectors.toList()));
            List<SequenceFlow> incomingFlows = FlowableUtil.getElementIncomingFlows(source);
            vo.setIncomingList(incomingFlows.stream().map(SequenceFlow::getId).collect(Collectors.toList()));
        }
        return vo;
    }
}
