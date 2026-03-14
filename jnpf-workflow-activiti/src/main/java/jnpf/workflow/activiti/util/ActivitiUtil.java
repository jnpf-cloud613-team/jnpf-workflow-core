package jnpf.workflow.activiti.util;

import org.activiti.bpmn.model.*;

import java.util.*;

/**
 * activiti工具类
 *
 * @author yanghuixing
 * @author YMPaaS Cloud@YinMai Info. Co., Ltd
 * @version 0.2.0
 * @since 2023/8/22 14:12
 */
public class ActivitiUtil {
    /**
     * 获取全部节点元素
     *
     * @param flowElements {@link Collection<FlowElement>}
     * @param allElements  {@link Collection<FlowElement>}
     * @return {@link Collection<FlowElement>}
     * @author yanghuixing
     * @since 2023/8/22 16:08
     **/
    public static Collection<FlowElement> getAllElements(Collection<FlowElement> flowElements, Collection<FlowElement> allElements) {
        allElements = allElements == null ? new ArrayList<>() : allElements;
        for (FlowElement flowElement : flowElements) {
            allElements.add(flowElement);
            if (flowElement instanceof SubProcess) {
                // 获取子流程元素
                allElements = getAllElements(((SubProcess) flowElement).getFlowElements(), allElements);
            }
        }
        return allElements;
    }

    /**
     * 获取节点的入口连线
     *
     * @param element {@link FlowElement}
     * @return {@link List<SequenceFlow>}
     * @author yanghuixing
     * @since 2023/8/22 16:09
     **/
    public static List<SequenceFlow> getElementIncomingFlows(FlowElement element) {
        List<SequenceFlow> sequenceFlows = null;
        if (element instanceof FlowNode) {
            sequenceFlows = ((FlowNode) element).getIncomingFlows();
        }
        return sequenceFlows;
    }

    /**
     * 获取节点的入口连线
     *
     * @param element {@link FlowElement}
     * @return {@link List<SequenceFlow>}
     * @author yanghuixing
     * @since 2023/8/22 16:10
     **/
    public static List<SequenceFlow> getElementOutgoingFlows(FlowElement element) {
        List<SequenceFlow> sequenceFlows = null;
        if (element instanceof FlowNode) {
            sequenceFlows = ((FlowNode) element).getOutgoingFlows();
        }
        return sequenceFlows;
    }

    /**
     * 获取可回退的节点（用户任务、子流程）
     *
     * @param source    {@link FlowElement}
     * @param passFlows {@link Set<String>}
     * @param passActs  {@link List<Activity>}
     * @return {@link List<Activity>}
     * @author yanghuixing
     * @since 2023/8/22 16:11
     **/
    public static List<Activity> getPassActs(FlowElement source, Set<String> passFlows, List<Activity> passActs) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        passActs = passActs == null ? new ArrayList<>() : passActs;

        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (null != sequenceFlows && sequenceFlows.size() > 0) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 连线重复
                if (passFlows.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加经过的连线
                passFlows.add(sequenceFlow.getId());
                // 添加经过的用户任务、子流程
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    passActs.add((UserTask) sequenceFlow.getSourceFlowElement());
                }
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    passActs.add((SubProcess) sequenceFlow.getSourceFlowElement());
                }
                // 迭代
                getPassActs(sequenceFlow.getSourceFlowElement(), passFlows, passActs);
            }
        }
        return passActs;
    }

    /**
     * 获取上一级节点（用户任务、子流程）
     *
     * @param source     {@link FlowElement}
     * @param passFlows  {@link Set<String>}
     * @param parentActs {@link List<Activity>}
     * @return {@link List<Activity>}
     * @author yanghuixing
     * @since 2023/8/22 16:13
     **/
    public static List<Activity> getParentActs(FlowElement source, Set<String> passFlows, List<Activity> parentActs) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        parentActs = parentActs == null ? new ArrayList<>() : parentActs;

        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (null != sequenceFlows && sequenceFlows.size() > 0) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 连线重复
                if (passFlows.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加经过的连线
                passFlows.add(sequenceFlow.getId());
                // 添加用户任务、子流程
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    parentActs.add((UserTask) sequenceFlow.getSourceFlowElement());
                    continue;
                }
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    parentActs.add((SubProcess) sequenceFlow.getSourceFlowElement());
                    continue;
                }
                // 迭代
                getParentActs(sequenceFlow.getSourceFlowElement(), passFlows, parentActs);
            }
        }
        return parentActs;
    }

    /**
     * 获取需要撤回的节点
     *
     * @param source         {@link FlowElement}
     * @param runTaskKeyList {@link List<String>}
     * @param passFlows      {@link Set<String>}
     * @param userTasks      {@link List<UserTask>}
     * @return {@link List<UserTask>}
     * @author yanghuixing
     * @since 2023/8/22 16:13
     **/
    public static List<UserTask> getChildUserTasks(FlowElement source, List<String> runTaskKeyList, Set<String> passFlows, List<UserTask> userTasks) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        userTasks = userTasks == null ? new ArrayList<>() : userTasks;
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (null != sequenceFlows && sequenceFlows.size() > 0) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 连线重复
                if (passFlows.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加经过的连线
                passFlows.add(sequenceFlow.getId());
                // 用户任务
                if (sequenceFlow.getTargetFlowElement() instanceof UserTask
                        && runTaskKeyList.contains(sequenceFlow.getTargetFlowElement().getId())) {
                    userTasks.add((UserTask) sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 子流程，从第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    FlowElement flowElement = (FlowElement) ((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0];
                    List<UserTask> tasks = getChildUserTasks(flowElement, runTaskKeyList, passFlows, null);
                    // 找到用户任务，不继续向下找
                    if (tasks.size() > 0) {
                        userTasks.addAll(tasks);
                        continue;
                    }
                }
                // 迭代
                getChildUserTasks(sequenceFlow.getTargetFlowElement(), runTaskKeyList, passFlows, userTasks);
            }
        }
        return userTasks;
    }

    /**
     * 获取下一级的用户任务
     *
     * @param source          {@link FlowElement}
     * @param hasSequenceFlow {@link Set<String>}
     * @param userTaskList    {@link List<UserTask>}
     * @return {@link List<UserTask>}
     * @author yanghuixing
     * @since 2023/8/22 16:15
     **/
    public static List<UserTask> getNextUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = Optional.ofNullable(hasSequenceFlow).orElse(new HashSet<>());
        userTaskList = Optional.ofNullable(userTaskList).orElse(new ArrayList<>());
        // 获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (null != sequenceFlows) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                if (targetFlowElement instanceof UserTask) {
                    // 若节点为用户任务，加入到结果列表中
                    userTaskList.add((UserTask) targetFlowElement);
                } else {
                    // 若节点非用户任务，继续递归查找下一个节点
                    getNextUserTasks(targetFlowElement, hasSequenceFlow, userTaskList);
                }
            }
        }
        return userTaskList;
    }

    /**
     * 判断某个节点的出口是否是网关，获取网关的出口连线
     *
     * @param source {@link FlowElement}
     * @return {@link List<SequenceFlow>}
     * @author yanghuixing
     * @since 2023/8/22 16:15
     **/
    public static List<SequenceFlow> getOutFlowsOfGateway(FlowElement source) {
        List<SequenceFlow> flows = new ArrayList<>();
        // 获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (null != sequenceFlows) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                if (targetFlowElement instanceof Gateway) {
                    List<SequenceFlow> outgoingFlows = ((Gateway) targetFlowElement).getOutgoingFlows();
                    flows.addAll(outgoingFlows);
                }
            }
            return flows;
        }
        return null;
    }

    /**
     * 获取之前的节点
     *
     * @param source    {@link FlowElement}
     * @param passFlows {@link Set<String>}
     * @param keys      {@link List<String>}
     * @return {@link List<String>}
     * @author yanghuixing
     * @since 2023/8/22 16:16
     **/
    public static List<String> getBefore(FlowElement source, Set<String> passFlows, List<String> keys) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        keys = keys == null ? new ArrayList<>() : keys;
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (null != sequenceFlows && sequenceFlows.size() > 0) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 连线重复
                if (passFlows.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加经过的连线
                passFlows.add(sequenceFlow.getId());
                // 添加节点Key
                keys.add(sequenceFlow.getSourceFlowElement().getId());
                if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {
                    continue;
                }
                // 迭代
                getBefore(sequenceFlow.getSourceFlowElement(), passFlows, keys);
            }
        }
        return keys;
    }
}
