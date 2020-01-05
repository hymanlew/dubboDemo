package com.hyman.dubbo.service.impl;

import java.util.List;

import com.hyman.dubbo.service.UserService;
import org.springframework.util.StringUtils;

import com.hyman.dubbo.bean.UserAddress;

/**
 * 本地存根：
 * 在实行远程服务后，客户端通常只剩下接口，而实现全在服务器端，但提供方有时想在客户端也执行部分逻辑，比如做 ThreadLocal 缓存，
 * 提前验证参数，调用失败后伪造容错数据等等。即在客户端调用提供端之前，先做一些判断或校验，如果满足则继续调用提供端。如果不满足
 * 则返回并处理自定义的逻辑。
 * 此时就需要在 API 中带上 Stub，客户端生成 Proxy 实例，会把 Proxy 通过构造函数传给 Stub，然后把 Stub 暴露给用户，Stub 可以决
 * 定要不要去调 Proxy。
 *
 * 在 spring 配置文件中按以下方式配置（在提供端，消费端都可以配置）：
 * <dubbo:service interface="com.foo.BarService" stub="true" /> 或 <dubbo:service interface="com.foo.BarService" stub="com.foo.BarServiceStub" />
 * <dubbo:reference interface="xxx.Service" id="xxService" timeout="5000" retries="3"	 version="*" stub="xxx.ServiceStub"></dubbo:reference>
 *
 * Stub 必须有可传入 Proxy 的构造函数。
 */
public class UserServiceStub implements UserService {
	
	private final UserService userService;

	/**
	 * dubbo 框架会自动传入，传入的是 userService 远程的代理对象，构造函数传入真正的远程代理对象
	 * @param userService
	 */
	public UserServiceStub(UserService userService) {
		super();
		this.userService = userService;
	}


    public List<UserAddress> getUserAddressList(String userId) {

		/**
		 * 此代码在客户端执行, 可以在客户端做 ThreadLocal 本地缓存，或预先验证参数是否合法，等等。
		 * 也可以使用 try-catch 容错，可以做任何 AOP 拦截事项。
 		 */
		System.out.println("UserService.....Stub....");
		if(!StringUtils.isEmpty(userId)) {
			return userService.getUserAddressList(userId);
		}
		return null;
	}

}