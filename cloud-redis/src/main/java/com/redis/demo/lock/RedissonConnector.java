package com.redis.demo.lock;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/19 下午12:51
 */
/**
 * 获取RedissonClient连接类
 */
@Component
public class RedissonConnector {
    RedissonClient redisson;
    @PostConstruct
    public void init(){
        redisson =Redisson.create();
    }
    public RedissonClient getClient(){
        return redisson;
    }
}
