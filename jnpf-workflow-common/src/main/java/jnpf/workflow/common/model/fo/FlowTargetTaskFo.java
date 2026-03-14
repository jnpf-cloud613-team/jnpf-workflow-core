package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 连接线目标任务
 *
 * @author JNPF@YinMai Info. Co., Ltd
 * @version 5.0.x
 * @since 2024/4/17 17:36
 */
@Data
public class FlowTargetTaskFo implements Serializable {
    /**
     * 部署ID
     */
    @Schema(name = "deploymentId", description = "部署ID")
    private String deploymentId;
    /**
     * 线的Key
     */
    @Schema(name = "flowKey", description = "线的Key")
    private String flowKey;
}
