package com.hyman.distributed.transaction.service.first.impl;

import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.common.utils.JWTUtil;
import com.hyman.distributed.transaction.pojo.dto.LoginDTO;
import com.hyman.distributed.transaction.pojo.entity.first.User;
import com.hyman.distributed.transaction.dao.first.mapper.UserMapper;
import com.hyman.distributed.transaction.pojo.vo.TokenInfoVO;
import com.hyman.distributed.transaction.pojo.vo.UserInfoVO;
import com.hyman.distributed.transaction.service.first.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hyman
 * @since 2019-10-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private JWTUtil jwtUtil;
    @Override
    public Result login(LoginDTO loginDTO) {
        User user = this.lambdaQuery()
                .select(User::getId, User::getName)
                .eq(User::getName, loginDTO.getName())
                .one();

        if (user == null) {
            return Result.fail(4001, "用户不存在！");
        }

        //省略业务逻辑...

        Map<String, Object> tokenData = new HashMap<>(2);
        tokenData.put("userId", user.getId());
        String token = jwtUtil.createJWT(tokenData);

        TokenInfoVO tokenInfoVO = new TokenInfoVO();
        tokenInfoVO.setToken(token);

        return Result.ok(tokenInfoVO);
    }
}
