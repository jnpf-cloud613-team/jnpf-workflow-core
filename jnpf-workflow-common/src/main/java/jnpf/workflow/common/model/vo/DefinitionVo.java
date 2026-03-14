package jnpf.workflow.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程定义VO
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/3 14:26
 */
@Data
public class DefinitionVo implements Serializable {
    /**
     * 定义ID
     */
    @Schema(name = "id", description = "定义ID")
    String definitionId;
    /**
     * 定义名称
     */
    @Schema(name = "name", description = "定义名称")
    String definitionName;
    /**
     * 定义Key
     */
    @Schema(name = "key", description = "定义Key")
    String definitionKey;
    /**
     * 定义版本
     */
    @Schema(name = "version", description = "定义版本")
    Integer definitionVersion;
    /**
     * 定义部署ID
     */
    @Schema(name = "deploymentId", description = "定义部署ID")
    String deploymentId;
}
