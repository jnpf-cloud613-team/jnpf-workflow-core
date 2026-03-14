package jnpf.workflow.flowable.service;

import cn.hutool.core.collection.CollectionUtil;
import jnpf.workflow.common.exception.BizException;
import jnpf.workflow.common.exception.ResultCode;
import jnpf.workflow.common.model.fo.InstanceDeleteFo;
import jnpf.workflow.common.model.fo.InstanceStartFo;
import jnpf.workflow.common.model.vo.HistoricInstanceVo;
import jnpf.workflow.common.model.vo.InstanceVo;
import jnpf.workflow.common.service.IInstanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

/**
 * 流程实例实现层
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 14:34
 */
@Slf4j
@Service
@AllArgsConstructor
public class InstanceServiceImpl implements IInstanceService {
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    @Override
    public InstanceVo startById(InstanceStartFo fo) {
        ProcessDefinition definition = repositoryService
                .createProcessDefinitionQuery()
                .deploymentId(fo.getDeploymentId()).singleResult();
        if (null == definition) {
            throw new BizException(ResultCode.DEFINITION_NOT_EXIST);
        }
        InstanceVo vo = new InstanceVo();
        ProcessInstance instance;
        if (CollectionUtil.isNotEmpty(fo.getVariables())) {
            instance = runtimeService.startProcessInstanceById(definition.getId(), fo.getVariables());
        } else {
            instance = runtimeService.startProcessInstanceById(definition.getId());
        }
        if (null != instance) {
            vo.setInstanceId(instance.getId());
        }
        return vo;
    }

    @Override
    public HistoricInstanceVo getHistoricProcessInstance(String processInstanceId) {
        HistoricProcessInstance historicInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (null == historicInstance) {
            throw new BizException(ResultCode.INSTANCE_NOT_EXIST);
        }
        HistoricInstanceVo vo = new HistoricInstanceVo();
        vo.setInstanceId(historicInstance.getId());
        vo.setStartTime(historicInstance.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        vo.setEndTime(historicInstance.getEndTime() == null ? null : historicInstance.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        vo.setDurationInMillis(historicInstance.getDurationInMillis());
        vo.setDeleteReason(historicInstance.getDeleteReason());
        return vo;
    }

    @Override
    public boolean deleteInstance(InstanceDeleteFo fo) {
        try {
            runtimeService.deleteProcessInstance(fo.getInstanceId(), fo.getDeleteReason());
            return true;
        } catch (Exception e) {
            log.error(ResultCode.DELETE_FAILURE.getMsg(), e);
        }
        return false;
    }
}
