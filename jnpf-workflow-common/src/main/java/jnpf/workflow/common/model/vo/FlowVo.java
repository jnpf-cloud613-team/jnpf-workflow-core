package jnpf.workflow.common.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 类的描述
 *
 * @author JNPF@YinMai Info. Co., Ltd
 * @version 5.0.x
 * @since 2024/8/13 13:31
 */
@Data
public class FlowVo {
    private String key;
    private List<FlowVo> children = new ArrayList<>();
}
