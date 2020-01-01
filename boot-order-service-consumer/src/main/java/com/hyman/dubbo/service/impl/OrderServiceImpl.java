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
     */
	//@Autowired
	@Reference(loadbalance="random", timeout=1000)
	UserService userService;
	
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
