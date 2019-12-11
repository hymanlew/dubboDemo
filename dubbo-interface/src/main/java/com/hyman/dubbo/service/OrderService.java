package com.hyman.dubbo.service;

import java.util.List;

import com.hyman.dubbo.bean.UserAddress;

public interface OrderService {
	
	/**
	 * 初始化订单
	 * @param userId
	 */
	List<UserAddress> initOrder(String userId);

}
