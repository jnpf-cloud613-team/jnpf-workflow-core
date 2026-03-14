package jnpf.workflow.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 元素Vo
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/8 17:48
 */
@Data
public class NodeElementVo implements Serializable {
    /**
     * 元素ID
     */
    @Schema(name = "id", description = "元素ID")
    private String id;
    /**
     * 元素名称
     */
    @Schema(name = "name", description = "元素名称")
    private String name;
    /**
     * 进线ID
     */
    @Schema(name = "incoming", description = "进线ID")
    private List<String> incomingList;
    /**
     * 出线ID
     */
    @Schema(name = "outgoingList", description = "出线ID")
    private List<String> outgoingList;
}
