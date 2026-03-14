package jnpf.workflow.common.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 类的描述
 *
 * @author JNPF@YinMai Info. Co., Ltd
 * @version 5.0.x
 * @since 2024/6/17 16:06
 */
@Data
public class HistoricNodeVo implements Serializable {
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 节点编码
     */
    private String code;
    /**
     * 开始时间
     */
    private Long startTime;
}
