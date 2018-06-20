package com.test.example.code1;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20 上午10:18
 */
@Mapper
public interface UserMapper1 {

    @Insert("insert into test_user(username,age) values(#{username},#{age})")
    void addUser(@Param("username")String username, @Param("age") int age);
}
