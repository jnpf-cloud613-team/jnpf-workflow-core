package jnpf.workflow.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 定义部署Vo
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 9:35
 */
@Data
public class DeploymentVo implements Serializable {
    /**
     * 部署ID
     */
    @Schema(name = "deploymentId", description = "部署ID")
    String deploymentId;
}
