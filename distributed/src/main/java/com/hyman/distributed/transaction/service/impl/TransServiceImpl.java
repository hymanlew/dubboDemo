package com.hyman.distributed.transaction.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyman.distributed.transaction.common.enums.ResultEnum;
import com.hyman.distributed.transaction.common.exception.BusinessException;
import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.dao.second.CustomerMapper;
import com.hyman.distributed.transaction.pojo.entity.Customer;
import com.hyman.distributed.transaction.pojo.entity.User;
import com.hyman.distributed.transaction.pojo.entity.UserDTO;
import com.hyman.distributed.transaction.service.LoginService;
import com.hyman.distributed.transaction.service.TransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现类
 *
 * @author hyman
 * @since 2019-10-05
 */
@Service
public class TransServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements TransService {

    @Autowired
    private LoginService userService;

    /**
     * 新增客户 - 演示多数据源分布式事务
     * @param userDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Result insert(UserDTO userDTO) {

        // 使用 lambdaQuery 查询，使代码更优雅。当然也可以直接使用本类对象 . 调用。
        Customer customer = this.lambdaQuery()
                .select(Customer::getUserId, Customer::getUserNum)
                .eq(Customer::getUserId, userDTO.getUserId())
                .one();

        if (customer == null) {
            return Result.fail(4001, "用户不存在！");
        }

        //添加客户
        User newuser = new User();
        newuser.setName(userDTO.getName())
                .setPhone(userDTO.getPhone());
        boolean userStatus = userService.save(newuser);

        //更新用户客户数
        boolean customerStatus = this.lambdaUpdate()
                .set(Customer::getUserNum, customer.getUserNum() + 1)
                .eq(Customer::getUserId, userDTO.getUserId())
                .update();

        // 如果不符合条件，则两个数据库表数据回滚
        if (! customerStatus || ! userStatus) {
            throw new BusinessException(ResultEnum.BUSINESS_ERROR);
        }

        return Result.ok();
    }
}
