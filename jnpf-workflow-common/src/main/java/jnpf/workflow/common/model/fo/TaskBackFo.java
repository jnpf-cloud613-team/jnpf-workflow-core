package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务退回参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 16:05
 */
@Data
public class TaskBackFo implements Serializable {
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @Schema(name = "taskId", description = "任务ID")
    private String taskId;
    /**
     * 目标节点ID
     */
    @Schema(name = "targetKey", description = "目标节点ID")
    private String targetKey;
}
