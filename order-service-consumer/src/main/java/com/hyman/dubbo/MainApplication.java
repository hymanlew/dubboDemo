package com.hyman.dubbo;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hyman.dubbo.service.OrderService;

/**
 * 服务提供者测试类，测试配置文件是否正确（前提是 zookeper，dubbo-admin 控制台都已经启动）
 */
public class MainApplication {
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("consumer.xml");
		
		OrderService orderService = applicationContext.getBean(OrderService.class);
		
		orderService.initOrder("1");
		System.out.println("调用完成....");
		System.in.read();
	}

}
