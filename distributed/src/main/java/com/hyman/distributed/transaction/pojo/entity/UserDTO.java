package com.hyman.distributed.transaction.pojo.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author hyman
 * @date 2019/10/6 11:49 上午
 */
@Data
public class UserDTO {

    @NotNull(message = "用户id不能为空")
    private String userId;

    @NotBlank(message = "用户名不能为空")
    private String name;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "客户手机号不能为空")
    private String phone;
}
