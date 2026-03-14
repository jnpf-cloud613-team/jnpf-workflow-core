package jnpf.workflow.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程实例Vo
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 14:42
 */
@Data
public class InstanceVo implements Serializable {
    /**
     * 实例ID
     */
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
}
