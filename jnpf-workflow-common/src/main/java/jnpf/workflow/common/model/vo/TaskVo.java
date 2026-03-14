package jnpf.workflow.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程任务VO
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 11:22
 */
@Data
public class TaskVo implements Serializable {
    /**
     * 任务ID
     */
    @Schema(name = "taskId", description = "任务ID")
    private String taskId;
    /**
     * 任务名称
     */
    @Schema(name = "taskName", description = "任务名称")
    private String taskName;
    /**
     * 任务Key
     */
    @Schema(name = "taskKey", description = "任务Key")
    private String taskKey;
    /**
     * 实例ID
     */
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
}
