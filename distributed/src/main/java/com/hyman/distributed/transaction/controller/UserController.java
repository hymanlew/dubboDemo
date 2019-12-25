package com.hyman.distributed.transaction.controller;


import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.entity.UserDTO;
import com.hyman.distributed.transaction.service.LoginService;
import com.hyman.distributed.transaction.service.TransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 用户控制器
 *
 * @author hyman
 * @since 2019-10-05
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private LoginService userService;

    @PostMapping("/login")
    public Result login(@Valid UserDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @Autowired
    private TransService transService;

    @PostMapping("/save")
    public Result store(@Valid UserDTO userDTO) {
        return transService.insert(userDTO);
    }
}

