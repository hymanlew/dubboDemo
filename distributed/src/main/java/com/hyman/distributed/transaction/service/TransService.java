package com.hyman.distributed.transaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.entity.Customer;
import com.hyman.distributed.transaction.pojo.entity.UserDTO;
import com.hyman.distributed.transaction.pojo.entity.User;

/**
 * 服务类
 *
 * @author hyman
 * @since 2019-10-05
 */
public interface TransService extends IService<Customer> {

    /**
     * 新增客户 - 演示多数据源分布式事务
     * @param userDTO
     * @return
     */
    Result insert(UserDTO userDTO);
}
