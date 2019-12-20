package com.hyman.distributed.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 分布式锁，使用 lua
 */
@Slf4j
@Component
public class DistributedLock2 {

    /**
     * 注意RedisTemplate用的String,String，后续所有用到的key和value都是String的
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * redis lua 脚本封装类
     */
    @Autowired
    RedisScript<Boolean> lockScript;
    @Autowired
    RedisScript<Long> unlockScript;

    public Boolean distributedLock(String key, String uuid, String secondsToLock) {

        Boolean locked = false;
        try {
            String millSeconds = String.valueOf(Integer.parseInt(secondsToLock) * 1000);

            /**
             * SpringBoot 自带 RedisTemplate 执行 lua 脚本以及预加载 lua 脚本到 Redis 集群：
             *
             * 引入 lua 脚本，放在和application.yml 同层目录下
             * DefaultRedisScript<List> redisScript = new DefaultRedisScript<List>();
             * redisScript.setLocation(new ClassPathResource("controller.lua"));
             * redisScript.setResultType(List.class);
             *
             * 运行lua脚本，keyList为传入的key值列表，需要加前缀，以确保在同一节点上，d为参数数组。
             * List<String> keyList = new ArrayList<>();
             * keyList.add("{pre}:door");
             * keyList.add("{pre}:dog");
             *
             * String [] d = new String[10];
             * d[0] = String.valueOf(12);
             * d[1] = "22";
             * List<String> result = redisUtils.getRedisTemplate().execute(redisScript,list,d);
             *
             * execute 方法的实现是：
             * 首先直接传sha值，如果在 Redis 中找不到预加载的 lua 脚本导致报错，则catch住该错误，把整个脚本序列化后传入Redis进行执行。
             *
             * 注意：excute方法必须明确传key值，否则报：Lua script attempted to access a non local key in a cluster node错误。
             * 因为 RedisTemplate 操作的是集群，redis需要通过key值确定槽和节点。多个key的话，key值前都需要加{前缀}：，以确保都在
             * 同一个节点上的槽。
             *
             * RedisTemplate 预加载 lua到 redis:
             * Lua 脚本运行之前都需要加载一次，为了减少网络开销，可在初始化时就将lua脚本预加载到redis中。通过redisTemplate 预加载lua脚本。
             * 返回的字符串为sha值。运行下面的代码后，再执行 redisUtils.getRedisTemplate().execute(redisScript,list,d)方法，源码中执行
             * 的就是第一步，是直接通过sha值运行Redis中提前加载好的lua脚本。
             *
             * String s =redisUtils.getRedisTemplate().getConnectionFactory().getClusterConnection().scriptLoad(redisSc
             */
            locked =redisTemplate.execute(lockScript, Collections.singletonList(key), uuid, millSeconds);

            log.info("distributedLock.key{}: - uuid:{}: - timeToLock:{} - locked:{} - millSeconds:{}",
                    key, uuid, secondsToLock, locked, millSeconds);

        } catch (Exception e) {

            log.error("error", e);
        }
        return locked;
    }

    public void distributedUnlock(String key, String uuid) {

        Long unlocked = redisTemplate.execute(unlockScript, Collections.singletonList(key), uuid);
        log.info("distributedLock.key{}: - uuid:{}: - unlocked:{}", key, uuid, unlocked);
    }

}
