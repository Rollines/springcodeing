package com.test.example.service;

import com.test.example.entity.User;
import com.test.example.code1.UserMapper1;
import com.test.example.code2.UserMapper2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20 上午11:00
 */
@Service
public class UserService {

    @Autowired
    private UserMapper1 userMapper1;
    @Autowired
    private UserMapper2 userMapper2;


    @Transactional
    public void addUser(User user) throws Exception{
        userMapper1.addUser(user.getUsername(),user.getAge());
        userMapper2.addUser(user.getUsername(),user.getAge());
    }
}
