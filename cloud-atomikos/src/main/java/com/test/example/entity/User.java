package com.test.example.entity;

import java.io.Serializable;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20 上午10:19
 */
public class User implements Serializable {
    private static final long serialVersionUID = 7832232236418948381L;
    private String username;
    private int age;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
