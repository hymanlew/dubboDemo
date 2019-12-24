package com.hyman.distributed.transaction.common.exception;


import com.hyman.distributed.transaction.common.enums.ResultEnum;

/**
 *  业务异常
 * @author hyman
 * @date 2019/4/3 11:09 上午
 */
public class BusinessException extends AbstractException {
    protected BusinessException(ResultEnum restEnum, Exception e) {
        super(restEnum, e);
    }

    public BusinessException(ResultEnum restEnum) {
        super(restEnum);
    }
}
