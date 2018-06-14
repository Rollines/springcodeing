package com.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/14 下午5:58
 */
@RestController
public class ApiController {
    @Qualifier("apiServiceError")
    @Autowired
    private ApiService apiService;

    @RequestMapping("index")
    public String index(){
        return apiService.index();
    }
}
