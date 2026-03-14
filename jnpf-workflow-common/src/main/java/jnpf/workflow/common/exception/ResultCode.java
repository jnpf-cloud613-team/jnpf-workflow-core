package jnpf.workflow.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回结果枚举类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/3 15:08
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResultCode implements Serializable {

    SUCCESS("200", "请求成功"),
    FAILURE("999", "请求失败"),

    DELETE_SUCCESS("1101", "删除成功"),
    DELETE_FAILURE("1102", "删除失败"),
    DEPLOY_FAILURE("1103", "部署失败"),
    START_FAILURE("1104", "启动失败"),
    COMPLETE_SUCCESS("1105", "任务完成成功"),
    COMPLETE_FAILURE("1106", "任务完成失败"),
    RETRACT_SUCCESS("1107", "撤回成功"),
    RETRACT_FAILURE("1108", "撤回失败"),
    JUMP_SUCCESS("1109", "跳转成功"),
    JUMP_FAILURE("1110", "跳转失败"),

    DEPLOY_ERROR("9001", "部署错误，请检查XML格式、内容等是否有误"),
    DEFINITION_NOT_EXIST("9002", "找不到流程模板，请重新发布该流程"),
    INSTANCE_NOT_EXIST("9003", "实例不存在"),
    TASK_NOT_EXIST("9004", "任务不存在"),
    TASK_COMPLETE_ERROR("9005", "任务完成错误"),
    TASK_JUMP_ERROR("9006", "节点跳转错误"),

    SYSTEM_EXECUTION_ERROR("9901", "系统执行出错"),
    REQUEST_PARAM_IS_NULL("9701", "请求必填参数为空");

    private String code;
    private String msg;

}
