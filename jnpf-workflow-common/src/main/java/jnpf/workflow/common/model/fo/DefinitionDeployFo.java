package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程定义部署参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/3 11:45
 */
@Data
public class DefinitionDeployFo implements Serializable {
    /**
     * bpmn xml字符串
     */
    @NotBlank(message = "bpmn xml字符串不能为空")
    @Schema(name = "bpmnXml", description = "bpmn xml字符串")
    private String bpmnXml;
    /**
     * 业务名称
     */
    @Schema(name = "name", description = "业务名称")
    private String name;
    /**
     * 业务Key
     */
    @Schema(name = "key", description = "业务Key")
    private String key;
}
