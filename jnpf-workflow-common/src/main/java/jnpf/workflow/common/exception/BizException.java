package jnpf.workflow.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author JNPF Flowable@YinMai Info. Co., Ltd
 * @version 1.0.0
 * @since 2024/4/3 15:31
 */
@Getter
public class BizException extends RuntimeException {
    public ResultCode resultCode;

    public BizException(ResultCode errorCode) {
        super(errorCode.getMsg());
        this.resultCode = errorCode;
    }

    public BizException(String message) {
        super(message);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }

    public BizException(Throwable cause) {
        super(cause);
        this.resultCode = ResultCode.SYSTEM_EXECUTION_ERROR;
    }
}
