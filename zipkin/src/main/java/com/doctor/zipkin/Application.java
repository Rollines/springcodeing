package com.doctor.zipkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.zipkin.stream.EnableZipkinStreamServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import zipkin.storage.mysql.MySQLStorage;

import javax.sql.DataSource;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/15 上午11:37
 */
@SpringBootApplication
@EnableZipkinStreamServer
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

    @Bean
    @Primary
    public MySQLStorage mySQLStorage(DataSource datasource)
    {
        return MySQLStorage.builder().datasource(datasource).executor(Runnable::run).build();
    }
}