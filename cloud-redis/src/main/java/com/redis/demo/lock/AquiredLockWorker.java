package com.redis.demo.lock;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/19 下午12:41
 */

/**
 * 获取锁后需要处理的逻辑
 * @param <T>
 */
public interface AquiredLockWorker<T> {
    T invokeAfterLockAquire() throws Exception;
}
