package com.hyman.distributed.transaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.entity.User;
import com.hyman.distributed.transaction.pojo.entity.UserDTO;

/**
 * 服务类
 *
 * @author hyman
 * @since 2019-10-05
 */
public interface LoginService extends IService<User> {

    /**
     * 用户登录
     * @param loginDTO
     * @return
     */
    Result login(UserDTO loginDTO);
}
