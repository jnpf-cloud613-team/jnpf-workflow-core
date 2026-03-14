package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 流程实例启动参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 15:14
 */
@Data
public class InstanceStartFo implements Serializable {
    /**
     * 部署ID
     */
    @NotBlank(message = "部署ID不能为空")
    @Schema(name = "deploymentId", description = "部署ID")
    private String deploymentId;
    /**
     * 变量
     */
    @Schema(name = "variables", description = "变量")
    private Map<String, Object> variables;
}
