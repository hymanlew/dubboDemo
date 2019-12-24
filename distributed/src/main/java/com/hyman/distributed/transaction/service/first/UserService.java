package com.hyman.distributed.transaction.service.first;

import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.dto.LoginDTO;
import com.hyman.distributed.transaction.pojo.entity.first.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hyman
 * @since 2019-10-05
 */
public interface UserService extends IService<User> {
    /**
     * 用户登录
     * @param loginDTO
     * @return
     */
    Result login(LoginDTO loginDTO);
}
