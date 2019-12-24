package com.hyman.distributed.transaction.web.controller.first;


import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.dto.LoginDTO;
import com.hyman.distributed.transaction.service.first.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 用户控制器
 * @author hyman
 * @since 2019-10-05
 */
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    public Result login(@Valid LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }
}

