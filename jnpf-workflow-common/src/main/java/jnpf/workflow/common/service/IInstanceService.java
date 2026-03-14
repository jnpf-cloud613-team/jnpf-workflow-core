package jnpf.workflow.common.service;


import jnpf.workflow.common.model.fo.InstanceDeleteFo;
import jnpf.workflow.common.model.fo.InstanceStartFo;
import jnpf.workflow.common.model.vo.HistoricInstanceVo;
import jnpf.workflow.common.model.vo.InstanceVo;

/**
 * 流程实例服务接口
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 14:31
 */
public interface IInstanceService {
    /**
     * 根据ID启动实例
     *
     * @param fo {@link InstanceStartFo}
     * @return {@link InstanceVo}
     * @since 2024/4/7 15:44
     **/
    InstanceVo startById(InstanceStartFo fo);

    /**
     * 获取历史流程实例
     *
     * @param processInstanceId {@link String}
     * @return {@link HistoricInstanceVo}
     * @since 2024/4/7 17:30
     **/
    HistoricInstanceVo getHistoricProcessInstance(String processInstanceId);

    /**
     * 删除流程实例
     *
     * @param fo {@link InstanceDeleteFo}
     * @return {@link boolean}
     * @since 2024/4/7 16:07
     **/
    boolean deleteInstance(InstanceDeleteFo fo);
}
