package com.hyman.distributed.lock.redisconf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.ResourceScriptSource;

@Slf4j
@Configuration
public class BeanConfiguration {

    /**
     * The script resultType should be one of Long, Boolean, List, or a deserialized value type. It can also be null if
     * the script returns a throw-away status (specifically, OK).
     *
     * 脚本定义的地方需要注意，返回的结果集一定是Long, Boolean，List, 一个反序列化的值。
     * @return
     */
    @Bean
    public RedisScript<Long> limitScript() {

        RedisScript redisScript = null;
        try {
            ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("/scripts/limit.lua"));
            log.info("script:{}", scriptSource.getScriptAsString());

            redisScript = RedisScript.of(scriptSource.getScriptAsString(), Long.class);

        } catch (Exception e) {
            log.error("error", e);
        }
        return redisScript;
    }

    @Bean
    public RedisScript<Boolean> lockScript() {

        RedisScript<Boolean> redisScript = null;
        try {
            ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("/scripts/lock.lua"));
            redisScript = RedisScript.of(scriptSource.getScriptAsString(), Boolean.class);

        } catch (Exception e) {
            log.error("error" , e);
        }
        return redisScript;
    }

    @Bean
    public RedisScript<Long> unlockScript() {

        RedisScript<Long> redisScript = null;
        try {
            ScriptSource scriptSource = new ResourceScriptSource(new ClassPathResource("/scripts/unlock.lua"));
            redisScript = RedisScript.of(scriptSource.getScriptAsString(), Long.class);

        } catch (Exception e) {
            log.error("error" , e);
        }
        return redisScript;
    }

    @Bean
    public RedisScript<Long> limitAnother() {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("/scripts/limit.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

}
