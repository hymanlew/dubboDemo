package com.hyman.distributed.lock;

import com.hyman.distributed.lock.lockconf.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的简单实现代码
 */
@Slf4j
@Component
public class DistributedLock {

    /**
     * connection = redisClient.getConnection();
     * Jedis jedis = (Jedis) connection.getShard(key);
     */

    private final JedisPool jedisPool;

    public DistributedLock(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 加锁：
     * 使用 redis 的 setNx 命令获取锁，保证设置值和失效时间是在同一条命令中执行。如果设置成功，则获取到锁，否则，视为获取不到锁。
     *
     * @param lockName       锁的key
     * @param acquireTimeout 获取超时时间，毫秒
     * @param timeout        锁的超时时间，毫秒
     * @return 锁标识
     */
    public String lockWithTimeout(String lockName, long acquireTimeout, long timeout) {

        Jedis jedis = null;
        String retIdentifier = null;

        /**
         * 分布式锁（基于 redis），可重入性：
         * 锁必须保证其可重入性，即一个方法获取了一个标识为 identifier 的锁，此时这个方法调用了另外一个方法，也需要 identifier的锁，
         * 此时由于当前线程已经有了这个锁，则不再获取直接进去，否则则会造成死锁。
         *
         * 可重入，并且此时已经获取到了一个相同的锁，则不再需要获取锁
         */
        if (ThreadUtils.getlockKey(lockName) != null) {
            if (log.isTraceEnabled()) {
                log.trace("[全局锁]当前已经获取到锁:" + lockName);
            }
            return "OK";
        }

        try {
            // 获取连接
            jedis = jedisPool.getResource();
            // 锁名，即key值
            String lockKey = "lock:" + lockName;
            // 随机生成一个value，作为锁的值
            String identifier = UUID.randomUUID().toString();
            // 超时时间，上锁后超过此时间则自动释放锁
            int lockExpire = (int) (timeout / 1000);

            // 获取锁的超时时间，超过这个时间则放弃获取锁
            long end = System.currentTimeMillis() + acquireTimeout;
            while (System.currentTimeMillis() < end) {

                /**
                 * 第三个参数：
                 * nx ： not exists, 只有key 不存在时才把key value set 到redis
                 * xx ： is exists ，只有 key 存在是，才把key value set 到redis
                 *
                 * 第四个参数：
                 * ex ： seconds 秒
                 * px :   milliseconds 毫秒
                 * 使用其他值，抛出 异常 ： redis.clients.jedis.exceptions.JedisDataException : ERR syntax error
                 */

                // 这个是 jedis 2.9.3 版本（旧版本）使用的方法。当前版本是 3.1.0
                //jedis.set("key","value","nx","ex",100);

                /**
                 * value需要使用一个唯一的值，这个值在解锁的时候需要判断是否一致，如果一致的话就进行解锁。这个也是官方推荐的方法。
                 */
                SetParams setParams = new SetParams();
                setParams.nx();
                setParams.ex(lockExpire);
                String result = jedis.set(lockKey, identifier, setParams);

                if ("OK".equals(result)) {
                    // 返回value值，用于释放锁时间确认
                    retIdentifier = identifier;
                    return retIdentifier;
                }

                // 如果获取锁失败则就阻塞等待一会
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait(1000);
                }

                // 另一种设置分布式锁的方法
                //if (jedis.setnx(lockKey, identifier) == 1) {
                //    jedis.expire(lockKey, lockExpire);
                //    // 返回value值，用于释放锁时间确认
                //    retIdentifier = identifier;
                //    return retIdentifier;
                //}
                //// 返回-1代表key没有设置超时时间，为key设置一个超时时间
                //if (jedis.ttl(lockKey) == -1) {
                //    jedis.expire(lockKey, lockExpire);
                //}

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return retIdentifier;
    }

    /**
     * 释放锁：
     * 1，只有加锁的人才能解锁。
     * 2，判断锁是否是自己的和释放这个锁必须是一个原子性操作。所以，释放锁必须编写 script 语句执行，不能先 get后 delete。
     *
     * @param lockName   锁的key
     * @param identifier 释放锁的标识
     * @return
     */
    public boolean releaseLock(String lockName, String identifier) {
        Jedis jedis = null;
        String lockKey = "lock:" + lockName;

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(identifier));
        // del 方法的返回值，是被删除 key 的数量
        if (!"1".equals(result)) {
            log.error("锁释放失败" + lockKey);
            return false;
        }
        return true;

        /**
         Redis 事务：
         特性：可以一次执行多个命令，本质是一组命令的集合。
         单独的隔离操作：一个事务中的所有命令都会被序列化，顺序地串行化执行而不会被其它命令插入，不许加塞。即事务执行过程中，不会被其他客户端发来的命令请求所打断。批量操作在发送 EXEC 命令前被放入队列缓存。

         没有隔离级别的概念：队列中的命令没有提交之前都不会实际的被执行，因为事务提交前任何指令都不被实际执行。也就不存在”事务内的查询要看到事务里的更新，在事务外查询不能看到“这个问题。

         不保证原子性：redis 同一个事务中如果有一条命令执行失败，其后的命令仍然会被执行，没有回滚。即部分支持事务。

         redis 事务的作用是：一个队列中，一次性，顺序性，排他性的执行一系列命令。在事务执行过程，其他客户端提交的命令请求不会插入到事务执行命令序列中。

         一个事务从开始到执行会经历以下三个阶段：开始事务，命令入队，执行事务。
         它先以 MULTI 开始一个事务， 然后将多个命令入队到事务中， 最后由 EXEC 命令触发事务， 一并执行事务中的所有命令：
         multi ， set hyman 'good man' ， get hyman ,  sadd set a b c d ， smembers set  ,  exec  执行如下：
         1) OK
         2) "good man"
         3) (integer) 4
         4) 1) "b"
         2) "a"
         3) "d"
         4) "c"

         全体连坐：收到 EXEC 命令后进入事务执行，一旦事务中任意命令执行失败，则其余的命令都将失败。
         冤头债主：执行 EXEC 命令进入事务执行后，一条命令执行失败，不影响其余命令的正常执行。
         两者的区别是：前者是在命令入队时就已经报错 error（就像 java 中的检查性异常），根本就到不了执行阶段，所以一旦执行就会直接被取消。而后者是命令正常（就像 java 中的非检查性异常，如 NPE），只有在执行过程中才会抛出异常，所以在异常之前或之后的命令执行不受影响。

         单个 Redis 命令的执行是原子性的，但 Redis 没有在事务上增加任何维持原子性的机制，所以 Redis 事务的执行并不是原子性的，即 redis 对事务是部分支持的，而不是像关系型数据库那样强一致性，要么全成功要么全失败。
         事务可以理解为一个打包的批量执行脚本，但批量指令并非原子化的操作，中间某条指令的失败不会导致前面已做指令的回滚，也不会造成后续的指令不做。

         multi，标记一个事务块的开始。
         exec，执行所有事务块内的命令。
         discard，取消事务，放弃执行事务块内的所有命令。
         watch key1 key2。。。，监视一个(或多个) key ，如果在事务执行之前这个(或这些) key 被其他命令所改动，那么事务将被打断。
         unwatch，取消 WATCH 命令对所有 key 的监视。

         watch 监控：
         在数据处理中，一致性与并发性总是会有冲突的。
         表锁：对数据是否会被修改很悲观，所以干脆把整张表给锁住。并发性极差，但是一致性极好。
         行锁：对数据是否会被修改很乐观，所以只把一行数据锁住。锁的范围就相对小多了，也保证了并发性。
         悲观锁：就类似于表锁，所以每次操作数据时都会上锁，其他人只能排队等着，即操作之前先上锁，多应用在关系型数据库中。
         乐观锁：是在操作的当前行末尾加上 version 标记，原理就像 SVN commit的原理。在更新数据时会先判断在此期间别人有没有在操作它，然后使用版本号的机制进行上锁。它是工作中用的最多的，适用于多读的应用类型，这样可以提高吞吐量。
         CAS（check and set）：检查后再设置。

         先监控然后再开启事务进行操作，如果在开始监控后，有其他人已经修改了数据，则自己的操作执行时就会失败，即事务执行失败。

         watch 指令类似于乐观锁，事务提交时如果 key 的值已被别的客户端改变，则整个事务队列都不会执行。通过 watch 命令在事务执行之前监控了多个 keys，若在 watch 之后有任何 key 的值发生了变化，则 exec执行的事务都将被放弃，同时返回 nullmulti-bulk 应答以通知调用者事务执行失败。

         如果没有人修改在事务成功执行后，就要 unwatch，以便于其他人进行修改。
         并且一旦执行了 exec后，则之前加的监控锁都会被取消掉。
         */
        //boolean retFlag = false;
        //try {
        //    jedis = jedisPool.getResource();
        //    while (true) {
        //        // 监视lock，准备开始事务
        //        jedis.watch(lockKey);
        //
        //        // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
        //        if (identifier.equals(jedis.get(lockKey))) {
        //            Transaction transaction = jedis.multi();
        //            transaction.del(lockKey);
        //            List<Object> results = transaction.exec();
        //            if (results == null) {
        //                continue;
        //            }
        //            retFlag = true;
        //        }
        //        jedis.unwatch();
        //        break;
        //    }
        //} catch (JedisException e) {
        //    e.printStackTrace();
        //} finally {
        //    if (jedis != null) {
        //        jedis.close();
        //    }
        //}
        //return retFlag;
    }


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 加锁
     * @param lockKey   商品id，作为锁
     * @param value 当前时间+超时时间
     * @return
     */
    public boolean lock(String lockKey, String value) {

        boolean lock = false;
        try {

            /**
             * redisTemplate.opsForValue();		//操作字符串
             * redisTemplate.opsForHash();		//操作hash
             * redisTemplate.opsForList();		//操作list
             * redisTemplate.opsForSet();		//操作set
             * redisTemplate.opsForZSet();		//操作有序set
             */
            //当值不存在时，则设置成功返回true
            lock = redisTemplate.opsForValue().setIfAbsent(lockKey, value);
            log.info("cancelCouponCode是否获取到锁：" + lock);

            if (lock) {
                //成功设置过期时间
                redisTemplate.expire(lockKey, 1, TimeUnit.MINUTES);
                return true;

            } else {
                /**
                 * 如果过期时间被设置成功，返回1。
                 * 如果设置失败或者key不存在，则返回0
                 *
                 * 在key上设置一个过期时间（timeout），一旦时间到了，这个key将会被自动删除。这种情况叫做 Redis 的不稳定性。
                 * 一旦设置了过期时间，这个key只能被命令清除、删除或者重写其内容。包含 del、set、getset 以及所有的 *store 命令。
                 * 这些命令只能改变key对应的value的存储值而不改变过期时间的设置。
                 *
                 * 举个例子，使用incr改变key对应的value、使用lpush添加一个新的元素到lists中、使用hset设置field对应value的值等等
                 * 这些操作都不影响已经对key设置的过期时间的属性。
                 *
                 * 设置了过期时间的key依然可以使用persist命令重新持久化。
                 * 如果key设置了过期时间，并且尚未被删除，使用rename命令重新命名后，该过期时间将转移到新的key上。事实上，rename命
                 * 令重命名key后，原始的key对应属性全部发生转移。
                 *
                 * 如果调用expire或者pexpire时传给一个负值作为参数以及expireat或者pexpireat调用的时候时间戳已经过去，那么该key将
                 * 直接被删除而不是等待过期。
                 *
                 * 刷新过期时间，对一个设置了过期时间的key仍然可以调用expire更新其过期时间。
                 *
                 * 新版本和低于2.1.3版本的区别，在2.1.3版本之前，如果设置时间触发了修改value的工作，则key将会被删除，这会影响主从
                 * 复制，之后的版本已经做了修正。
                 */

                //避免死锁，且只让一个线程拿到锁
                String currentValue = redisTemplate.opsForValue().get(lockKey);

                //如果加锁的线程占用超时了，即虽然没过期，但是锁上面的时间已经超时了
                if (!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {

                    //以下的操作会导致原来的锁，解锁异常
                    //获取上一个锁的时间并设置新值，设置键的字符串值并返回其旧值
                    String oldValues = redisTemplate.opsForValue().getAndSet(lockKey, value);
                    redisTemplate.expire(lockKey, 1, TimeUnit.MINUTES);
                    return true;
                }
                return false;
            }

        } catch (Exception e){
            log.error(e.getMessage());

        } finally {
            if (lock) {
                redisTemplate.delete(lockKey);
                log.info("cancelCouponCode任务结束，释放锁!");
                lock = true;
            } else {
                log.info("cancelCouponCode没有获取到锁，无需释放锁!");
                lock = false;
            }
        }
        return lock;
    }


    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key, String value) {

        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("『redis分布式锁』解锁异常，{}", e);
        }
    }
}
