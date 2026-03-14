package jnpf.workflow.flowable.util;

import cn.hutool.core.collection.CollectionUtil;
import jnpf.workflow.common.model.vo.FlowVo;
import org.flowable.bpmn.model.*;

import java.util.*;

/**
 * flowable工具类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 11:06
 */
public class FlowableUtil {
    /**
     * 获取全部节点元素
     *
     * @param flowElements {@link Collection<FlowElement>}
     * @param allElements  {@link Collection<FlowElement>}
     * @return {@link Collection<FlowElement>}
     * @since 2024/4/8 11:07
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
     * @since 2024/4/8 11:10
     **/
    public static List<SequenceFlow> getElementIncomingFlows(FlowElement element) {
        List<SequenceFlow> flows = null;
        if (element instanceof FlowNode) {
            flows = ((FlowNode) element).getIncomingFlows();
        }
        return flows;
    }

    /**
     * 获取节点的出口连线
     *
     * @param element {@link FlowElement}
     * @return {@link List<SequenceFlow>}
     * @since 2024/4/8 11:10
     **/
    public static List<SequenceFlow> getElementOutgoingFlows(FlowElement element) {
        List<SequenceFlow> flows = null;
        if (element instanceof FlowNode) {
            flows = ((FlowNode) element).getOutgoingFlows();
        }
        return flows;
    }

    /**
     * 获取可回退的节点（仅用户任务）
     *
     * @param source    {@link FlowElement}
     * @param passFlows {@link Set<String>}
     * @param passActs  {@link List<String>}
     * @return {@link List<String>}
     * @since 2024/4/8 15:27
     **/
    public static List<String> getPassActs(FlowElement source, Set<String> passFlows, List<String> passActs) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        passActs = passActs == null ? new ArrayList<>() : passActs;

        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (null != sequenceFlows && !sequenceFlows.isEmpty()) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 连线重复
                if (passFlows.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加经过的连线
                passFlows.add(sequenceFlow.getId());
                // 添加经过的用户任务
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    passActs.add(sequenceFlow.getSourceFlowElement().getId());
                }
                if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {
                    passActs.add(sequenceFlow.getSourceFlowElement().getId());
                    continue;
                }
                // 迭代
                getPassActs(sequenceFlow.getSourceFlowElement(), passFlows, passActs);
            }
        }
        return passActs;
    }

    /**
     * 获取需要撤回的节点
     *
     * @param source         {@link FlowElement}
     * @param runTaskKeyList {@link List<String>}
     * @param passFlows      {@link Set<String>}
     * @param userTasks      {@link List<UserTask>}
     * @return {@link List<UserTask>}
     * @since 2024/4/8 15:42
     **/
    public static List<UserTask> getChildUserTasks(FlowElement source, List<String> runTaskKeyList, Set<String> passFlows, List<UserTask> userTasks) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        userTasks = userTasks == null ? new ArrayList<>() : userTasks;
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (null != sequenceFlows && !sequenceFlows.isEmpty()) {
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
                    if (!tasks.isEmpty()) {
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
     * 获取上一级节点
     *
     * @param source     {@link FlowElement}
     * @param passFlows  {@link Set<String>}
     * @param parentActs {@link List<String>}
     * @return {@link List<Activity>}
     * @since 2024/4/8 15:53
     **/
    public static List<String> getParentActs(FlowElement source, Set<String> passFlows, List<String> parentActs) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        parentActs = parentActs == null ? new ArrayList<>() : parentActs;

        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (null != sequenceFlows && !sequenceFlows.isEmpty()) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 连线重复
                if (passFlows.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加经过的连线
                passFlows.add(sequenceFlow.getId());
                // 添加用户任务、子流程
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    parentActs.add(sequenceFlow.getSourceFlowElement().getId());
                    continue;
                }
                if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {
                    parentActs.add(sequenceFlow.getSourceFlowElement().getId());
                    continue;
                }
                // 迭代
                getParentActs(sequenceFlow.getSourceFlowElement(), passFlows, parentActs);
            }
        }
        return parentActs;
    }

    /**
     * 获取下一级的用户任务
     *
     * @param source          {@link FlowElement}
     * @param hasSequenceFlow {@link Set<String>}
     * @param userTaskList    {@link List<UserTask>}
     * @return {@link List<UserTask>}
     * @since 2024/4/8 16:34
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
     * 获取元素之后的所有节点
     *
     * @param source          {@link FlowElement}
     * @param hasSequenceFlow {@link Set<String>}
     * @param list            {@link List<String>}
     * @return {@link List<String>}
     * @since 2024/4/29 16:00
     **/
    public static List<String> getAfter(FlowElement source, Set<String> hasSequenceFlow, List<String> list) {
        hasSequenceFlow = Optional.ofNullable(hasSequenceFlow).orElse(new HashSet<>());
        list = Optional.ofNullable(list).orElse(new ArrayList<>());
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
                    list.add(targetFlowElement.getId());
                } else if (targetFlowElement instanceof EndEvent) {
                    list.add(targetFlowElement.getId());
                    continue;
                }
                // 继续递归查找下一个节点
                getAfter(targetFlowElement, hasSequenceFlow, list);
            }
        }
        return list;
    }

    /**
     * 获取节点的出口连线，若出线的出口是网关，则一并获取网关的出线
     *
     * @param source {@link FlowElement}
     * @return {@link List<SequenceFlow>}
     * @since 2024/4/9 9:58
     **/
    public static List<SequenceFlow> getOutFlowsWithGateway(FlowElement source, List<SequenceFlow> flows) {
        flows = flows == null ? new ArrayList<>() : flows;
        // 获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (null != sequenceFlows) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                flows.add(sequenceFlow);
                FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                if (targetFlowElement instanceof UserTask) {
                    continue;
                }
                if (targetFlowElement instanceof Gateway) {
                    Gateway gateway = (Gateway) targetFlowElement;
                    List<SequenceFlow> outgoingFlows = gateway.getOutgoingFlows();
                    flows.addAll(outgoingFlows);
                    getOutFlowsWithGateway(gateway, flows);
                }
            }
        }
        return flows;
    }

    /**
     * 获取出线，上下级关系
     *
     * @param source 源
     * @param flows  结果集合
     */
    public static List<FlowVo> getOutFlows(FlowElement source, List<FlowVo> flows) {
        flows = flows == null ? new ArrayList<>() : flows;
        // 获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (null != sequenceFlows) {
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                FlowVo vo = new FlowVo();
                vo.setKey(sequenceFlow.getId());

                FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
                if (targetFlowElement instanceof Gateway) {
                    Gateway gateway = (Gateway) targetFlowElement;
                    List<FlowVo> list = getOutFlows(gateway, null);
                    vo.setChildren(list);
                }
                flows.add(vo);
            }
        }
        return flows;
    }

    /**
     * 获取之前的节点
     *
     * @param source    {@link FlowElement}
     * @param passFlows {@link Set<String>}
     * @param keys      {@link List<String>}
     * @return {@link List<String>}
     * @since 2024/4/9 11:51
     **/
    public static List<String> getBefore(FlowElement source, Set<String> passFlows, List<String> keys) {
        passFlows = passFlows == null ? new HashSet<>() : passFlows;
        keys = keys == null ? new ArrayList<>() : keys;
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (null != sequenceFlows && !sequenceFlows.isEmpty()) {
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

    /**
     * 获取线之后的任务节点
     *
     * @return
     */
    public static String getTaskKeyAfterFlow(FlowElement source) {
        if (source instanceof SequenceFlow) {
            SequenceFlow sequenceFlow = (SequenceFlow) source;
            FlowElement target = sequenceFlow.getTargetFlowElement();
            if (target instanceof Gateway) {
                List<SequenceFlow> outgoingFlows = ((Gateway) target).getOutgoingFlows();
                if (CollectionUtil.isNotEmpty(outgoingFlows)) {
                    SequenceFlow flow = outgoingFlows.get(0);
                    return getTaskKeyAfterFlow(flow);
                }
            }
            if (target instanceof UserTask) {
                return target.getId();
            }
        }
        return null;
    }
}
