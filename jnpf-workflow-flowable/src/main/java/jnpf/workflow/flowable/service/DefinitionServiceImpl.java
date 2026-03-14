package jnpf.workflow.flowable.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jnpf.workflow.common.exception.BizException;
import jnpf.workflow.common.exception.ResultCode;
import jnpf.workflow.common.model.fo.DefinitionDeleteFo;
import jnpf.workflow.common.model.fo.DefinitionDeployFo;
import jnpf.workflow.common.model.vo.DefinitionVo;
import jnpf.workflow.common.model.vo.DeploymentVo;
import jnpf.workflow.common.model.vo.FlowElementVo;
import jnpf.workflow.common.service.IDefinitionService;
import jnpf.workflow.flowable.util.FlowableUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义实现层
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/3 11:36
 */
@Slf4j
@Service
@AllArgsConstructor
public class DefinitionServiceImpl implements IDefinitionService {
    private final RepositoryService repositoryService;

    @Override
    public DeploymentVo deployDefinition(DefinitionDeployFo fo) {
        Deployment deployment;
        try {
            String resourceName;
            if (StrUtil.isNotBlank(fo.getKey())) {
                resourceName = fo.getKey();
            } else {
                resourceName = IdUtil.getSnowflakeNextIdStr();
            }
            deployment = repositoryService
                    .createDeployment()
                    .name(fo.getName())
                    .key(fo.getKey())
                    .addString(resourceName + ".bpmn20.xml", fo.getBpmnXml())
                    .disableSchemaValidation()
                    .deploy();
        } catch (Exception e) {
            throw new BizException(ResultCode.DEPLOY_ERROR.getMsg(), e);
        }
        DeploymentVo vo = new DeploymentVo();
        vo.setDeploymentId(deployment.getId());
        return vo;
    }

    @Override
    public List<DefinitionVo> listDefinition() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
        List<DefinitionVo> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(definitions)) {
            for (ProcessDefinition definition : definitions) {
                DefinitionVo vo = new DefinitionVo();
                vo.setDefinitionId(definition.getId());
                vo.setDefinitionName(definition.getName());
                vo.setDefinitionKey(definition.getKey());
                vo.setDefinitionVersion(definition.getVersion());
                vo.setDeploymentId(definition.getDeploymentId());
                list.add(vo);
            }
        }
        return list;
    }

    @Override
    public boolean deleteDefinition(DefinitionDeleteFo fo) {
        if (null == fo.getCascade()) {
            fo.setCascade(true);
        }
        try {
            // 根据部署ID删除，并级联删除当前流程定义下的所有流程实例、job
            repositoryService.deleteDeployment(fo.getDeploymentId(), fo.getCascade());
            return true;
        } catch (Exception e) {
            log.error(ResultCode.DELETE_FAILURE.getMsg(), e);
        }
        return false;
    }

    @Override
    public List<FlowElementVo> getStructure(String deploymentId) {
        List<FlowElementVo> vos = new ArrayList<>();

        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        if (null != definition) {
            Process process = repositoryService.getBpmnModel(definition.getId()).getProcesses().get(0);
            Collection<FlowElement> elements = FlowableUtil.getAllElements(process.getFlowElements(), null);
            for (FlowElement element : elements) {
                FlowElementVo vo = BeanUtil.copyProperties(element, FlowElementVo.class);
                if (element instanceof Event) {
                    Event el = (Event) element;
                    vo.setIncomingList(el.getIncomingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                    vo.setOutgoingList(el.getOutgoingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                }
                if (element instanceof Activity) {
                    Activity el = (Activity) element;
                    vo.setIncomingList(el.getIncomingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                    vo.setOutgoingList(el.getOutgoingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                }
                if (element instanceof Gateway) {
                    Gateway el = (Gateway) element;
                    vo.setIncomingList(el.getIncomingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                    vo.setOutgoingList(el.getOutgoingFlows().stream().map(SequenceFlow::getId).collect(Collectors.toList()));
                }
                vos.add(vo);
            }
        }

        return vos;
    }
}
