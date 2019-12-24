package com.hyman.distributed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分布式锁三种方案的比较：
 * 无论哪种方式都无法做到完美。就像CAP一样，在复杂性、可靠性、性能等方面无法同时满足，所以根据不同的应用场景选择最适合自己的才是王道。
 *
 * Zookeeper：
 * 优点：有封装好的框架，容易实现。具备高可用、可重入、阻塞锁特性（等待队列），可解决失效死锁问题，大大提升抢锁效率。
 * 缺点：因为需要频繁的创建和删除节点，性能上不如 Redis 方式。
 * 集群自己来保证数据一致性，但是会存在建立无用节点且多节点之间需要同步数据的问题，因此一般适合于并发量小的场景使用，例如定时任务的运
 * 行等。
 *
 * Redis：
 * 优点：set 和 del 指令的性能较高。
 * 缺点：实现复杂，需要考虑超时，原子性，误删等情形。没有等待锁的队列，只能在客户端自旋来等徜，效率低下。
 * （非redlock）由于redis的高性能原因，会有很好的性能，但是极端情况下会存在两个客户端获取锁（可以通过监控leader故障和运维措施来缓解和
 * 解决该问题），因此适用于高并发的场景。
 *
 * 并且两者都可以在客户端实现可重入锁的逻辑。从可靠性角度（从高到低）：Zookeeper > 缓存 > 数据库。
 * 在分布式环境中，对资源进行上锁有时候是很重要的，比如抢购某一资源，这时候使用分布式锁就可以很好地控制资源。当然在具体使用中，还需要
 * 考虑很多因素，比如超时时间的选取，获取锁时间的选取对并发量都有很大的影响。
 */
@SpringBootApplication
public class DistributedApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedApplication.class, args);
    }

}
