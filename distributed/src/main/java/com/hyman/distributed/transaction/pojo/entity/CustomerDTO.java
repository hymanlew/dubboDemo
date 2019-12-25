package com.hyman.distributed.transaction.pojo.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author hyman
 * @date 2019/10/6 11:49 上午
 */
@Data
public class CustomerDTO {

    @NotNull(message = "用户id不能为空")
    private String userId;

    @NotBlank(message = "用户数不能为空")
    private String userNum;
}
