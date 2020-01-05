package com.hyman.dubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.ImportResource;

import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;

/**
 * 1、导入依赖：导入 dubbo-starter，导入 dubbo 的其他依赖。
 *
 * SpringBoot 与 dubbo 整合的三种方式：
 * 1）、导入dubbo-starter，在 application.properties、dubbo.properties 中配置属性，使用 @EnableDubbo，@Service 暴露服务，使用 @Reference 引用服务。
 * 2）、保留 dubbo.xml 配置文件，	导入 dubbo-starter，使用 @ImportResource 导入 dubbo 的配置文件，并把 @Service 注解去掉。
 * 3）、使用 @Configuration 注解的方式：将每一个组件手动创建到容器中，让 dubbo 来扫描其他的组件。并把 @Service 注解去掉。使用 @EnableDubbo(scanBasePackages) 来扫描配置类。
 */

// 开启基于注解的dubbo功能，即开启 dubbo 的包扫描
//@EnableDubbo

//@ImportResource(locations="classpath:provider.xml")

@EnableDubbo(scanBasePackages="com.hyman.dubbo")

// 开启服务容错功能
@EnableHystrix
@SpringBootApplication
public class BootUserServiceProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootUserServiceProviderApplication.class, args);
	}
}
