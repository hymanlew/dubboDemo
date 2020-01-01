package com.hyman.dubbo.service.impl;

import java.util.List;

import com.hyman.dubbo.service.UserService;
import org.springframework.util.StringUtils;

import com.hyman.dubbo.bean.UserAddress;

public class UserServiceStub implements UserService {
	
	private final UserService userService;

	/**
	 * 传入的是userService远程的代理对象
	 * @param userService
	 */
	public UserServiceStub(UserService userService) {
		super();
		this.userService = userService;
	}


    public List<UserAddress> getUserAddressList(String userId) {

		System.out.println("UserServiceStub.....");
		if(!StringUtils.isEmpty(userId)) {
			return userService.getUserAddressList(userId);
		}
		return null;
	}

}