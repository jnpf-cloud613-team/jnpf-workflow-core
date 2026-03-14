package jnpf.workflow.common.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 类的描述
 *
 * @author JNPF@YinMai Info. Co., Ltd
 * @version 5.0.x
 * @since 2024/6/11 10:33
 */
@Data
public class FlowElementVo {
    private String id;
    private String name;
    /**
     * 线的源
     */
    private String sourceRef;
    /**
     * 线的目标
     */
    private String targetRef;
    /**
     * 节点进线
     */
    private List<String> incomingList;
    /**
     * 节点出线
     */
    private List<String> outgoingList;
}
