package com.hyman.distributed.lock.lockconf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义 limit 注解，限流注解，并且设置注解的key和限流的大小
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistriLimitAnno {

    String limitKey() default "limit";

    int limit() default 1;
}
