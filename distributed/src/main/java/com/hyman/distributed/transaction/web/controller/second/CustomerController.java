package com.hyman.distributed.transaction.web.controller.second;


import com.hyman.distributed.transaction.common.response.Result;
import com.hyman.distributed.transaction.pojo.dto.StoreCustomerDTO;
import com.hyman.distributed.transaction.service.second.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hyman
 * @since 2019-10-05
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @PostMapping("")
    public Result store(@Valid StoreCustomerDTO storeCustomerDTO) {
        return customerService.store(storeCustomerDTO);
    }
}

