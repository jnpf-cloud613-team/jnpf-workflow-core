package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 任务完成参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 13:57
 */
@Data
public class TaskCompleteFo implements Serializable {
    /**
     * 任务ID
     */
    @NotBlank(message = "任务ID不能为空")
    @Schema(name = "taskId", description = "任务ID")
    private String taskId;
    /**
     * 变量
     */
    @Schema(name = "variables", description = "变量")
    private Map<String, Object> variables;
}
