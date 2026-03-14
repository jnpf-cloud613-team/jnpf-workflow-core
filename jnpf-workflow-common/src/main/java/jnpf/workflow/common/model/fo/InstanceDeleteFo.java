package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程实例删除参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 15:55
 */
@Data
public class InstanceDeleteFo implements Serializable {
    /**
     * 实例ID
     */
    @NotBlank(message = "实例ID不能为空")
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
    /**
     * 删除原因
     */
    @Schema(name = "deleteReason", description = "删除原因")
    private String deleteReason;
}
