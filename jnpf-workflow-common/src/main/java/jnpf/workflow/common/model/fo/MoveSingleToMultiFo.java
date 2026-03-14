package jnpf.workflow.common.model.fo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单节点跳转多节点参数类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 16:36
 */
@Data
public class MoveSingleToMultiFo implements Serializable {
    /**
     * 实例ID
     */
    @NotBlank(message = "实例ID不能为空")
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
    /**
     * 当前节点
     */
    @NotBlank(message = "当前节点不能为空")
    @Schema(name = "sourceKey", description = "当前节点")
    private String sourceKey;
    /**
     * 目标节点集合
     */
    @NotNull(message = "目标节点集合不能为空")
    @Schema(name = "targetKeys", description = "目标节点集合")
    private List<String> targetKeys;
}
