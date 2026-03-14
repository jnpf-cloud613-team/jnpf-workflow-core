package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 多节点跳转单节点参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/9 14:23
 */
@Data
public class MoveMultiToSingleFo implements Serializable {
    /**
     * 实例ID
     */
    @NotBlank(message = "实例ID不能为空")
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
    /**
     * 当前节点集合
     */
    @NotNull(message = "当前节点集合不能为空")
    @Schema(name = "sourceKeys", description = "当前节点集合")
    private List<String> sourceKeys;
    /**
     * 目标节点
     */
    @NotBlank(message = "目标节点不能为空")
    @Schema(name = "targetKey", description = "目标节点")
    private String targetKey;
}
