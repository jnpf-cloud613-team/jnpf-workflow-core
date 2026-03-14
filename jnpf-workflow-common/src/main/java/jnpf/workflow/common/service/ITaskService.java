package jnpf.workflow.common.service;


import jnpf.workflow.common.model.fo.*;
import jnpf.workflow.common.model.vo.FlowVo;
import jnpf.workflow.common.model.vo.HistoricNodeVo;
import jnpf.workflow.common.model.vo.NodeElementVo;
import jnpf.workflow.common.model.vo.TaskVo;

import java.util.List;

/**
 * 流程任务服务接口
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 11:11
 */
public interface ITaskService {

    /**
     * 根据流程实例ID获取任务
     *
     * @param instanceId {@link String}
     * @return {@link List<TaskVo>}
     * @since 2024/4/8 14:15
     **/
    List<TaskVo> getTask(String instanceId);

    /**
     * 完成任务
     *
     * @param fo {@link TaskCompleteFo}
     * @return {@link boolean}
     * @since 2024/4/8 14:21
     **/
    boolean complete(TaskCompleteFo fo);

    /**
     * 单节点跳转多节点
     *
     * @param fo {@link MoveSingleToMultiFo}
     * @return {@link boolean}
     * @since 2024/4/9 14:48
     **/
    boolean moveSingleToMulti(MoveSingleToMultiFo fo);

    /**
     * 多节点跳转单节点
     *
     * @param fo {@link MoveMultiToSingleFo}
     * @return {@link boolean}
     * @since 2024/4/9 14:48
     **/
    boolean moveMultiToSingle(MoveMultiToSingleFo fo);

    /**
     * 节点跳转
     *
     * @param fo {@link JumpFo}
     * @return {@link boolean}
     * @since 2024/4/10 11:35
     **/
    boolean jump(JumpFo fo);

    /**
     * 获取可回退的节点ID
     *
     * @param taskId {@link String}
     * @return {@link List<String>}
     * @since 2024/4/8 15:39
     **/
    List<String> getFallbacks(String taskId);

    /**
     * 回退目标节点
     *
     * @param fo {@link TaskBackFo}
     * @return {@link List<String>}
     * @since 2024/4/8 16:11
     **/
    List<String> back(TaskBackFo fo);

    /**
     * 获取上一级任务节点ID集合，用于自动处置的相邻选项
     *
     * @param fo {@link TaskPrevFo}
     * @return {@link List<String>}
     * @since 2024/4/8 16:30
     **/
    List<String> getPrevUserTask(TaskPrevFo fo);

    /**
     * 获取下一级任务节点集合
     *
     * @param fo {@link TaskNextFo}
     * @return {@link List< NodeElementVo >}
     * @since 2024/4/9 9:20
     **/
    List<NodeElementVo> getNextUserTask(TaskNextFo fo);

    /**
     * 获取线之后的任务节点
     *
     * @param fo {@link FlowTargetTaskFo}
     * @return {@link String}
     * @since 2024/4/17 17:45
     **/
    List<String> getTaskKeyAfterFlow(FlowTargetTaskFo fo);

    /**
     * 撤回
     *
     * @param taskId {@link String}
     * @return {@link boolean}
     * @since 2024/4/9 9:30
     **/
    boolean retract(String taskId);

    /**
     * 获取出线Key集合（若出线的出口为网关，则一并获取网关的出线）
     *
     * @param fo {@link TaskOutgoingFo}
     * @return {@link List<String>}
     * @since 2024/4/9 10:54
     **/
    List<String> getOutgoingFlows(TaskOutgoingFo fo);

    /**
     * 获取出线
     *
     * @param fo 参数
     */
    List<FlowVo> getOutgoing(TaskOutgoingFo fo);

    /**
     * 获取完成的节点Key
     *
     * @param instanceId {@link String}
     * @return {@link List<String>}
     * @since 2024/4/9 13:51
     **/
    List<String> getKeysOfFinished(String instanceId);

    /**
     * 获取进线的Key
     *
     * @param taskId {@link String}
     * @return {@link List<String>}
     * @since 2024/4/9 13:56
     **/
    List<String> getIncomingFlows(String taskId);

    /**
     * 获取未经过的节点
     *
     * @param instanceId {@link String}
     * @return {@link List<String>}
     * @since 2024/4/29 10:01
     **/
    List<String> getToBePass(String instanceId);

    /**
     * 获取节点的后续节点
     *
     * @param fo 参数
     */
    List<String> getAfter(TaskAfterFo fo);

    /**
     * 异常补偿
     *
     * @param fo 参数
     */
    List<TaskVo> compensate(CompensateFo fo);

    /**
     * 获取历史节点
     *
     * @param instanceId 实例主键
     */
    List<HistoricNodeVo> getHistoric(String instanceId);

    /**
     * 获取历史结束节点
     *
     * @param instanceId 实例主键
     */
    List<String> getHistoricEnd(String instanceId);

    /**
     * 获取元素信息
     *
     * @param model 参数
     */
    NodeElementVo getElementInfo(InfoModel model);
}
