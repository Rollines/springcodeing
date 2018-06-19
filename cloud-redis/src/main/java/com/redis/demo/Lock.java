package com.redis.demo;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/19 上午10:41
 * 创建一个全局的锁
 */
public class Lock {
    private String name;
    private String value;

    public Lock(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
