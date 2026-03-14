package jnpf.workflow.common.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 历史流程实例Vo
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/7 17:04
 */
@Data
public class HistoricInstanceVo implements Serializable {
    /**
     * 实例ID
     */
    @Schema(name = "instanceId", description = "实例ID")
    private String instanceId;
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(name = "startTime", description = "开始时间")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(name = "endTime", description = "结束时间")
    private LocalDateTime endTime;
    /**
     * 耗时
     */
    @Schema(name = "durationInMillis", description = "耗时")
    private Long durationInMillis;
    /**
     * 删除原因
     */
    @Schema(name = "deleteReason", description = "删除原因")
    private String deleteReason;
}
