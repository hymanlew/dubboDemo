package com.hyman.dubbo.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hyman.dubbo.bean.UserAddress;
import com.hyman.dubbo.service.OrderService;
import com.hyman.dubbo.service.UserService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * 1、将服务提供者注册到注册中心（暴露服务）
 * 		1）、导入dubbo依赖（2.6.2）\操作zookeeper的客户端(curator)
 * 		2）、配置服务提供者
 * 
 * 2、让服务消费者去注册中心订阅服务提供者的服务地址，dubbo.registry.address。
 *
 * @author hyman
 * @date 2019/12/01
 */
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 使用注解声明需要调用的远程服务的接口；生成远程服务代理。名字必须与服务提供者暴露的接口名称相同。使用 dubbo 直连。
	 * 同时还可以设置 check = false，关闭某个服务的启动时检查 (没有提供者时报错)。
	 *
	 * 高可用（即通过设计，减少系统不能提供服务的时间）：
	 * 1，zookeeper宕机与dubbo直连。现象：zookeeper注册中心宕机，还可以消费dubbo暴露的服务。原因（健壮性）：
	 *
	 * 监控中心宕掉不影响使用，只是丢失部分采样数据。
	 * 数据库宕掉后，注册中心仍能通过缓存提供服务列表查询，但不能注册新服务。
	 * 注册中心对等集群，任意一台宕掉后，将自动切换到另一台。
	 * 注册中心全部宕掉后，服务提供者和服务消费者仍能通过本地缓存通讯。
	 * 服务提供者无状态，任意一台宕掉后，不影响使用。
	 * 服务提供者全部宕掉后，服务消费者应用将无法使用，并无限次重连等待服务提供者恢复。
	 *
	 * 因为注册中心的作用，就是为了保存服务提供者的位置信息的。所以即使没有注册中心，也可以通过 dubbo直连实现服务调用。
	 * 在引用提供者的服务时，可以设置 @Reference(url="127.0.0.1:8081")，
	 *
	 *
	 * 2，集群下dubbo负载均衡配置。在集群负载均衡时，Dubbo 提供了多种均衡策略，缺省为 random 随机调用。负载均衡策略：
	 * Random LoadBalance，随机，按权重设置随机概率。在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也
	 * 比较均匀，有利于动态调整提供者权重。
	 *
	 * RoundRobin LoadBalance，轮循，按公约后的权重设置轮循比率。存在慢的提供者累积请求的问题，比如一台机器很慢，但没挂，当请
	 * 求调到该机器时就卡在那，久而久之，所有请求都卡在这台机器上。
	 *
	 * LeastActive LoadBalance，最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。使慢的提供者收到更少请求，因为越慢
	 * 的提供者的调用前后计数差会越大。
	 *
	 * ConsistentHash LoadBalance，一致性 Hash，相同参数的请求总是发到同一提供者。当某一台提供者挂时，原本发往该提供者的请求，
	 * 基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
	 * 算法参见：http://en.wikipedia.org/wiki/Consistent_hashing。
	 * 缺省只对第一个参数 Hash，如果要修改，请配置 <dubbo:parameter key="hash.arguments" value="0,1" />
	 * 缺省用 160 份虚拟节点，如果要修改，请配置 <dubbo:parameter key="hash.nodes" value="320" />
	 *
	 * 配置时，可以在提供端 <dubbo:service loadbalance=“xxx”>，消费端 <dubbo:reference loadbalance=“xxx”>。也可以配置在方法
	 * 级别上。或者在使用 @Reference(loadbalance="random", timeout=1000) 引用服务时。
	 *
	 * 对于提供者的权重配置，可以使用 admin 控制台中的功能进行设置。
     */
	//@Autowired
	@Reference(loadbalance="random", timeout=1000)
	UserService userService;

	/**
	 * 3，整合hystrix，服务熔断与降级处理。
	 * 服务降级：当服务器压力剧增的情况下，根据实际业务情况及流量，对一些服务和页面有策略的不处理或换种简单的方式处理，从而释放
	 * 服务器资源以保证核心交易正常运作或高效运作。可以通过服务降级功能临时屏蔽某个出错的非关键服务，并定义降级后的返回策略。
	 * 共有两种方式实现：
	 * mock=force:return+null 表示消费方对该服务的方法调用都直接返回 null 值，不发起远程调用。用来屏蔽不重要服务不可用时对调用方的影响。
	 * mock=fail:return+null 表示消费方对该服务的方法调用在失败后，再返回 null 值，不抛异常。用来容忍不重要服务不稳定时对调用方的影响。
	 * 在 dubbo admin 控制台中的功能进行设置。
	 *
	 * 整合hystrix：Hystrix 旨在通过控制那些访问远程系统、服务和第三方库的节点，从而对延迟和故障提供更强大的容错能力。Hystrix
	 * 具备拥有回退机制和断路器功能的线程和信号隔离，请求缓存和请求打包，以及监控和配置等功能。
	 * @param userId
	 * @return
	 */
	@HystrixCommand(fallbackMethod="hello")
	@Override
	public List<UserAddress> initOrder(String userId) {

		System.out.println("用户id："+userId);

		// 查询用户的收货地址
		List<UserAddress> addressList = userService.getUserAddressList(userId);
		return addressList;
	}
	
	
	public List<UserAddress> hello(String userId) {
	
		return Arrays.asList(new UserAddress(10, "测试地址", "1", "测试", "测试", "Y"));
	}
	

}
