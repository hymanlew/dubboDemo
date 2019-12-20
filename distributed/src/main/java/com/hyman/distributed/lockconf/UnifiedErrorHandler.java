package com.hyman.distributed.lockconf;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一的controller错误处理，
 * 如果controller出现Exception的时候都需要走这块异常。如果是正常的RunTimeException的时候获取一下，否则将异常获取一下并且输出。
 */
@ControllerAdvice
public class UnifiedErrorHandler {

    private static Map<String, String> res = new HashMap<>(2);

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object processException(HttpServletRequest req, Exception e) {

        res.put("url", req.getRequestURL().toString());

        if(e instanceof RuntimeException) {
            res.put("mess", e.getMessage());
        } else {
            res.put("mess", "sorry error happens");
        }
        return res;
    }
}
