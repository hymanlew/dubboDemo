package com.hyman.distributed.lock.lockconf;

import com.hyman.distributed.lock.DistributedLimit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 对注解进行切面，在切面中判断是否超过limit，如果超过limit的时候就需要抛出异常exceeded limit，否则正常执行。
 */
@Aspect
@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class LimitAspect {


    @Autowired
    DistributedLimit distributedLimit;

    @Pointcut("@annotation(com.hqs.distributedlock.annotation.DistriLimitAnno)")
    public void limit() {};

    @Before("limit()")
    public void beforeLimit(JoinPoint joinPoint) throws Exception {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistriLimitAnno distriLimitAnno = method.getAnnotation(DistriLimitAnno.class);

        String key = distriLimitAnno.limitKey();
        int limit = distriLimitAnno.limit();
        Boolean exceededLimit = distributedLimit.distributedLimit(key, String.valueOf(limit));

        /**
         * 如果已经限流了
         */
        if(!exceededLimit) {
            throw new RuntimeException("exceeded limit");
        }
    }

}
