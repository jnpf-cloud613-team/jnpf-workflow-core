package jnpf.workflow.common.service;


import jnpf.workflow.common.model.fo.DefinitionDeleteFo;
import jnpf.workflow.common.model.fo.DefinitionDeployFo;
import jnpf.workflow.common.model.vo.DefinitionVo;
import jnpf.workflow.common.model.vo.DeploymentVo;
import jnpf.workflow.common.model.vo.FlowElementVo;

import java.util.List;

/**
 * 流程定义服务接口
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/3 11:36
 */
public interface IDefinitionService {
    /**
     * 部署流程定义
     *
     * @param fo {@link DefinitionDeployFo}
     * @return {@link DeploymentVo}
     * @since 2024/4/7 10:51
     **/
    DeploymentVo deployDefinition(DefinitionDeployFo fo);

    /**
     * 列表查询流程定义
     *
     * @return {@link List<DefinitionVo>}
     * @since 2024/4/7 11:23
     **/
    List<DefinitionVo> listDefinition();

    /**
     * 删除流程定义
     *
     * @param fo {@link DefinitionDeleteFo}
     * @return {@link boolean}
     * @since 2024/4/7 13:51
     **/
    boolean deleteDefinition(DefinitionDeleteFo fo);

    /**
     * 获取流程元素
     *
     * @param deploymentId 部署ID
     */
    List<FlowElementVo> getStructure(String deploymentId);
}
