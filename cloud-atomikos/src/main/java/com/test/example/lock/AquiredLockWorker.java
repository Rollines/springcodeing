package com.test.example.lock;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20
 */

/**
 * 获取锁后需要处理的逻辑
 * @param <T>
 */
public interface AquiredLockWorker<T> {
    T invokeAfterLockAquire() throws Exception;
}
