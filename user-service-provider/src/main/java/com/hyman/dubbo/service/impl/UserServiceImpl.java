package com.hyman.dubbo.service.impl;

import java.util.Arrays;
import java.util.List;

import com.hyman.dubbo.bean.UserAddress;
import com.hyman.dubbo.service.UserService;

public class UserServiceImpl implements UserService {

    public List<UserAddress> getUserAddressList(String userId) {

        System.out.println("UserServiceImpl.....1...");

        UserAddress address1 = new UserAddress(1, "北京市昌平区宏福科技园综合楼3层", "1", "李老师", "010-56253825", "Y");
        UserAddress address2 = new UserAddress(2, "深圳市宝安区西部硅谷大厦B座3层（深圳分校）", "1", "王老师", "010-56253825", "N");

        // 模拟消息端调用时，提供者运行时间长，造成的超时
        //try {
		//	Thread.sleep(4000);
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//}

        return Arrays.asList(address1, address2);
    }

}
