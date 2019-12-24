package com.hyman.distributed.transaction.service.second;

import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.dto.StoreCustomerDTO;
import com.hyman.distributed.transaction.pojo.entity.second.Customer;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hyman
 * @since 2019-10-05
 */
public interface CustomerService extends IService<Customer> {
    /**
     * 新增客户 - 演示多数据源分布式事务
     * @param storeCustomerDTO
     * @return
     */
    Result store(StoreCustomerDTO storeCustomerDTO);
}
