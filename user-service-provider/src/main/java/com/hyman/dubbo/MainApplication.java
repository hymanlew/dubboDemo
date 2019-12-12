package com.hyman.dubbo;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 服务提供者测试类，测试配置文件是否正确（前提是 zookeper，dubbo-admin 控制台都已经启动）
 */
public class MainApplication {
	
	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext ioc = new ClassPathXmlApplicationContext("provider.xml");
		ioc.start();

		// 为了避免主程序运行完就退出，在此阻塞等待一个字符的输入
		System.in.read();
	}

}
