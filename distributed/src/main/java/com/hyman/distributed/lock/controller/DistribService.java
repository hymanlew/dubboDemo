package com.hyman.distributed.lock.controller;

import com.hyman.distributed.lock.DistributedLock2;
import com.hyman.distributed.lock.lockconf.DistriLimitAnno;
import com.hyman.distributed.lock.redisconf.RestTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 * 本事例采用了 lua 脚本和 Redis 实现了锁和限流，但是真实使用的时候还需要多测试，另外如果此次Redis也是采用的单机实现方法，使
 * 用集群的时候可能需要改造一下。
 *
 * 关于锁这块其实 Reids 自己也实现了 RedLock, java实现的版本 Redission。也有很多公司使用了，功能非常强大。各种场景下都用到了。
 */
@Slf4j
@Controller
public class DistribService {

    @Autowired
    private DistributedLock2 lock2;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/distributedLock")
    @ResponseBody
    public String distributedLock(String key, String uuid, String secondsToLock, String userId) throws Exception{

        //String uuid = UUID.randomUUID().toString();
        Boolean locked = false;

        try {
            locked = lock2.distributedLock(key, uuid, secondsToLock);

            if(locked) {
                log.info("userId:{} is locked - uuid:{}", userId, uuid);
                log.info("do business logic");
                TimeUnit.MICROSECONDS.sleep(3000);
            } else {
                log.info("userId:{} is not locked - uuid:{}", userId, uuid);
            }

        } catch (Exception e) {
            log.error("error", e);

        } finally {
            if(locked) {
                lock2.distributedUnlock(key, uuid);
            }
        }
        return "ok";
    }

    /**
     * 测试方法，用于测试和输出结果, 使用100个线程，然后锁的时间设置10秒，controller里边需要休眠3秒模拟业务执行。
     */
    @PostMapping("/distributedLockTest")
    public void distrubtedLock() {

        String url = "http://localhost:8080/distributedLock";
        String uuid = "abcdefg";
        log.info("uuid:{}", uuid);

        String key = "redisLock";
        String secondsToLive = "10";

        for(int i = 0; i < 100; i++) {

            final int userId = i;
            new Thread(() -> {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("uuid", uuid);
                params.add("key", key);
                params.add("secondsToLock", secondsToLive);
                params.add("userId", String.valueOf(userId));

                String result = RestTemplateUtil.getInstance().postForObject(url, params, String.class);
                System.out.println("-------------" + result);
            }
            ).start();
        }
    }

    /**
     * 将注解写到自定义的 controller 上，limit的大小为10，也就是10秒钟内限制10次访问。
     * @param userId
     * @return
     */
    @PostMapping("/distributedLimit")
    @ResponseBody
    @DistriLimitAnno(limitKey="limit", limit = 10)
    public String distributedLimit(String userId) {

        log.info(userId);
        return "ok";
    }
}
