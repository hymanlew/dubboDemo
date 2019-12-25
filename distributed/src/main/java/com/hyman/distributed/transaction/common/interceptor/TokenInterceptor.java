package com.hyman.distributed.transaction.common.interceptor;

import com.hyman.distributed.transaction.common.exception.TokenException;
import com.hyman.distributed.transaction.common.utils.JWTUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * token 拦截器
 *
 * @author hyman
 * @date 2019/10/6 10:16 上午
 */
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {

        // 获取请求头token
        String token = request.getHeader("Access-Token");
        if (StringUtils.isBlank(token)) {
            throw new TokenException();
        }

        // 获取token的数据
        Map tokenData = jwtUtil.getJWTData(token);
        if (tokenData == null) {
            throw new TokenException();
        }

        // 验证 token 逻辑省略...

        return true;
    }
}
