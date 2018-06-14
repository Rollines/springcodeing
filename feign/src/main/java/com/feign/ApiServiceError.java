package com.feign;

import org.springframework.stereotype.Component;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/14 下午5:57
 */
@Component
public class ApiServiceError implements ApiService{
    @Override
    public String index() {
        return "服务发生故障！";
    }
}
