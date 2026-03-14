package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程定义删除参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 11:37
 */
@Data
public class DefinitionDeleteFo implements Serializable {
    /**
     * 引擎部署ID
     */
    @NotBlank(message = "引擎部署ID不能为空")
    @Schema(name = "deploymentId", description = "引擎部署ID")
    private String deploymentId;
    /**
     * 是否级联删除流程定义下的流程实例等
     */
    @Schema(name = "cascade", description = "是否级联删除流程定义下的流程实例等")
    private Boolean cascade;
}
