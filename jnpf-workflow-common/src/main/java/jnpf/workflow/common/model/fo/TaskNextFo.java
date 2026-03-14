package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 下一级任务参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 17:03
 */
@Data
public class TaskNextFo implements Serializable {
    /**
     * 部署ID
     */
    @Schema(name = "deploymentId", description = "部署ID")
    private String deploymentId;
    /**
     * 节点Key
     */
    @Schema(name = "taskKey", description = "节点Key")
    private String taskKey;
    /**
     * 任务ID
     */
    @Schema(name = "taskId", description = "任务ID")
    private String taskId;
}
