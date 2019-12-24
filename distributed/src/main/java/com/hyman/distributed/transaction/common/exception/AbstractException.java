package com.hyman.distributed.transaction.common.exception;


import com.hyman.distributed.transaction.common.enums.ResultEnum;
import lombok.Getter;

/**
 * 抽象异常
 * @author hyman
 * @date 2019/4/2 11:09 上午
 */

@Getter
public abstract class AbstractException extends RuntimeException {

    /** 响应码 */
    protected final int code;
    /** 响应消息 */
    protected final String msg;

    protected AbstractException(ResultEnum restEnum, Exception e) {
        super(restEnum.getMsg(), e);
        this.code = restEnum.getCode();
        this.msg = restEnum.getMsg();
    }

    protected AbstractException(ResultEnum restEnum) {
        super(restEnum.getMsg());
        this.code = restEnum.getCode();
        this.msg = restEnum.getMsg();
    }
    protected AbstractException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
