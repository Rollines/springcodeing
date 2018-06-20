package com.test.example.lock;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20
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
