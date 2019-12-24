package com.hyman.distributed.transaction.common.exception;

import com.hyman.distributed.transaction.common.enums.ResultEnum;

/**
 * token 异常
 * @author hyman
 * @date 2019/10/6 10:19 上午
 */
public class TokenException extends AbstractException {
    public TokenException() {
        super(ResultEnum.TOKEN_ERROR.getCode(), ResultEnum.TOKEN_ERROR.getMsg());
    }
}
