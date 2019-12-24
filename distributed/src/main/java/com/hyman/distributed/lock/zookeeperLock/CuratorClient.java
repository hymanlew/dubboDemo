package com.hyman.distributed.lock.zookeeperLock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Service;

/**
 * 基于Zookeeper实现分布式锁；
 * ZooKeeper是一个为分布式应用提供一致性服务的开源组件，它内部是一个分层的文件系统目录树结构，这棵树由节点组成，称做Znode。并
 * 规定同一个节点下只能有一个唯一节点名。
 *
 * Znode分为四种类型：
 * 1，持久节点（PERSISTENT），默认的节点类型。创建节点的客户端与 zookeeper 断开连接后，该节点依旧存在 。
 * 2，持久节点顺序节点（PERSISTENT_SEQUENTIAL），所谓顺序节点，就是在创建节点时，Zookeeper 根据创建的时间顺序给该节点名称进行编号。
 * 3，临时节点（EPHEMERAL），和持久节点相反，当创建节点的客户端与zookeeper断开连接后，临时节点会被删除。
 * 4，临时顺序节点（EPHEMERAL_SEQUENTIAL），即它结合了临时节点和顺序节点的特点：在创建节点时，Zookeeper根据创建的时间顺序给该
 * 节点名称进行编号；当创建节点的客户端与zookeeper断开连接后，临时节点会被删除。
 *
 * 基于ZooKeeper实现分布式锁的步骤如下：
 * （1）在Zookeeper当中创建一个持久节点 ParentLock。
 *
 * （2）当客户端 Client1 线程进入此节点之后，就同步监听 /ParentLock 下序号比自己小的节点的事件。同时线程想要获得锁时，需要在
 *      ParentLock 节点下面创建一个临时顺序节点 Lock1。
 *
 * （3）之后 Client1查找ParentLock下面所有的临时顺序节点并按序号排序，判断自己所创建的节点Lock1是不是顺序最小的一个。如果是第
 *      一个节点，则成功获得锁。
 *
 * （4）这时如果再有一个客户端 Client2 前来获取锁，则在ParentLock下载再创建一个临时顺序节点Lock2。Client2查找ParentLock下面所
 *      有的临时顺序节点并排序，判断自己所创建的节点Lock2是不是顺序最靠前的一个，结果发现节点Lock2并不是最小的。于是 Client2向
 *      排序仅比它靠前的节点Lock1注册Watcher，用于监听Lock1节点是否存在。这意味着Client2抢锁失败，进入了等待状态。
 *
 * （5）这时如果又有一个客户端Client3前来获取锁，则在ParentLock下载再创建一个临时顺序节点Lock3。Client3查找ParentLock下面所有
 *      的临时顺序节点并排序，判断自己所创建的节点Lock3是不是顺序最靠前的一个，结果同样发现节点Lock3并不是最小的。于是，Client3
 *      向排序仅比它靠前的节点Lock2注册Watcher，用于监听Lock2节点是否存在。这意味着Client3同样抢锁失败，进入了等待状态。
 *
 * （6）如此 Client1得到了锁，Client2监听了Lock1，Client3监听了Lock2。这恰恰形成了一个等待队列，很像是Java当中ReentrantLock所
 *      依赖的AQS（AbstractQueuedSynchronizer）。
 *
 * AQS（java.util.concurrent.locks.AbstractQueuedSynchronizer）是用来构建锁或者其他同步组件（信号量、事件等）的基础框架类。JDK中许多并发工具类的内部实现都依
 * 赖于AQS，如ReentrantLock, Semaphore, CountDownLatch等等（AQS 是它们的父类，所以它们也称为 AQS 的同步组件）。
 * AQS的主要使用方式是继承它作为一个内部辅助类实现同步原语，它可以简化你的并发工具的内部实现，屏蔽同步状态管理、线程的排队、等待与唤醒等底层操作。
 *
 * AQS内部维护一个CLH队列来管理锁。线程会首先尝试获取锁，如果失败，则将当前线程以及等待状态等信息包成一个Node节点加到同步队列里。接着会不断循环尝试获取锁
 * （条件是当前节点为head的直接后继才会尝试）,如果失败则会阻塞自己，直至被唤醒；而当持有锁的线程释放锁时，会唤醒队列中的后继线程。
 *
 *
 * 释放锁分为两种情况：
 * 1，任务完成，客户端显示释放。当任务完成时，Client1会显示调用删除节点Lock1的指令。
 * 2，任务执行过程中，客户端崩溃。获得锁的Client1在任务执行过程中，如果崩溃，则会断开与Zookeeper服务端的链接。根据临时节点的特性，相关联的节点Lock1会随之自动删除。
 * 由于Client2一直监听着Lock1的存在状态，当Lock1节点被删除，Client2会立刻收到通知。这时候Client2会再次查询ParentLock下面的所有节点，确认自己创建的节点Lock2是不是目前最小的节点。如
 * 果是最小，则Client2顺理成章获得了锁。
 * 同理，如果Client2也因为任务完成或者节点崩溃而删除了节点Lock2，那么Client3就会接到通知。最终，Client3成功得到了锁。
 *
 * zookeeper实现分布式锁的原理就是多个节点同时在一个指定的节点下面创建临时会话顺序节点，谁创建的节点序号最小，谁就获得了锁，并且其他节点就会监听序号比自己小的节点，一旦序号比自己小
 * 的节点被删除了，其他节点就会得到相应的事件，然后查看自己是否为序号最小的节点，如果是，则获取锁。
 * 这里推荐一个Apache的开源库Curator，它是一个ZooKeeper客户端，Curator提供的InterProcessMutex是分布式锁的实现，acquire方法用于获取锁，release方法用于释放锁。
 */
@Slf4j
@Service
public class CuratorClient {

    public static void main(String[] args) {

        // 创建分布式请求的客户端
        //RetryPolicy retryPolicy = new RetryNTimes(3, 1000);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client1 = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
        client1.start();

        CuratorFramework client2 = CuratorFrameworkFactory.newClient("111.231.83.101:2181",retryPolicy);
        client2.start();

        // 创建分布式锁, 锁空间的根节点路径为/curator/lock
        InterProcessMutex mutex  = new InterProcessMutex(client1,"/curator/lock");
        final InterProcessMutex mutex2  = new InterProcessMutex(client2,"/curator/lock");

        // client1 获取锁，并进行业务流程
        try {
            mutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("clent Enter mutex");
        Thread client2Th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // client2 获取锁，并进行业务流程
                    //mutex2.acquire(seconds, TimeUnit.SECONDS);
                    mutex2.acquire();
                    System.out.println("client2 Enter mutex");
                    mutex2.release();
                    System.out.println("client2 release lock");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        client2Th.start();

        // client1 完成业务流程, 主动显示调用释放锁
        try {
            Thread.sleep(5000);
            mutex.release();
            System.out.println("client release lock");

            // 由于是 main 主线程调用的 client2Th 线程的 join 方法，所以 main 主线程会等待 client2Th 执行完之后才再往下执行。
            client2Th.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //关闭客户端
        client1.close();
    }
}