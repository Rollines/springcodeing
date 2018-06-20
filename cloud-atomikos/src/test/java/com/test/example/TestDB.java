package com.test.example;

import com.test.example.entity.User;
import com.test.example.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20 上午11:11
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class TestDB {

    @Autowired
    private UserService userService;

    @Test
    public void test(){
        User user = new User();
        user.setUsername("Doctor.chen");
        user.setAge(30);
        try {
            userService.addUser(user);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}