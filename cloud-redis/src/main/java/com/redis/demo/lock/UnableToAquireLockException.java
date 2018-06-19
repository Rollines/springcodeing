package com.redis.demo.lock;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/19 下午12:47
 */
public class UnableToAquireLockException extends RuntimeException {
    public UnableToAquireLockException(){

    }
    public UnableToAquireLockException(String message) {
        super(message);
    }

    public UnableToAquireLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
