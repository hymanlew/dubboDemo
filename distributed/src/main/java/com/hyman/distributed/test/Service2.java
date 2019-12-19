package com.hyman.distributed.test;

import com.hyman.distributed.lock.DistributedLock2;
import com.hyman.distributed.redisconf.Logutil;
import com.hyman.distributed.redisconf.RestTemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.Charset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class Service2 {

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
                Logutil.logger.info("userId:{} is locked - uuid:{}", userId, uuid);
                Logutil.logger.info("do business logic");
                TimeUnit.MICROSECONDS.sleep(3000);
            } else {
                Logutil.logger.info("userId:{} is not locked - uuid:{}", userId, uuid);
            }

        } catch (Exception e) {
            Logutil.logger.error("error", e);

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
    public void distrubtedLock() {

        String url = "http://localhost:8080/distributedLock";
        String uuid = "abcdefg";
        Logutil.logger.info("uuid:{}", uuid);

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
}
