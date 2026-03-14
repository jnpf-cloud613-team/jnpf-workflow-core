package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 跳转参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/10 11:18
 */
@Data
public class JumpFo implements Serializable {
    /**
     * 实例ID
     */
    @NotBlank(message = "实例ID不能为空")
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
    /**
     * 源节点集合
     */
    @Schema(name = "source", description = "源节点集合")
    private List<String> source;
    /**
     * 目标节点集合
     */
    @Schema(name = "target", description = "目标节点集合")
    private List<String> target;
}
