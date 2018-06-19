package com.redis.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;


/**
 * @Author: Doctor.chen
 * @Date: 2018/6/19 上午10:43
 * 创建一个分布式锁类
 */
@Component
public class DistributedLockHandler {
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockHandler.class);
    //单个业务持有锁的时间30秒，防止锁死
    private final static long LOCK_EXPIRE = 30*1000L;
    //默认30ms尝试一次
    private final static long LOCK_TRY_INTERVAL = 30L;
    //默认尝试20s
    private final static long LOCK_TRY_TIMEOUT = 20 * 1000L;

    @Autowired
    private StringRedisTemplate template;

    /**
     * 尝试获取全局的锁
     * @param lock 锁的名称
     * @return true 成功，false失败
     */
    public boolean tryLock(Lock lock){
        return getLock(lock,LOCK_EXPIRE,LOCK_TRY_INTERVAL,LOCK_TRY_TIMEOUT);
    }


    /**
     *
     * @param lock
     * @param timeout 获取超时时间 单位ms
     * @return true 获取成功，false获取失败
     */

    public boolean tryLock(Lock lock,long timeout){
        return getLock(lock,timeout,LOCK_EXPIRE,LOCK_TRY_INTERVAL);
    }

    /**
     *
     * @param lock
     * @param timeout  获取锁的超时时间
     * @param tryInterval 多少毫秒尝试获取一次
     * @return
     */
    public boolean tryLock(Lock lock,long timeout,long tryInterval){
        return getLock(lock,timeout,tryInterval,LOCK_EXPIRE);
    }


    /**
     *
     * @param lock
     * @param timeout  获取的超时时间
     * @param tryInterval  多少ms尝试一次
     * @param lockExpireTime  获取成功后锁的过期时间
     * @return
     */
    public boolean getLock(Lock lock, long timeout, long tryInterval, long lockExpireTime) {
        try{
            if (StringUtils.isEmpty(lock.getName())||StringUtils.isEmpty(lock.getValue())){
                return false;
            }
            long startTime = System.currentTimeMillis();
            do {
                if (!template.hasKey(lock.getName())){
                    ValueOperations<String,String> ops = template.opsForValue();
                    ops.set(lock.getName(),lock.getValue(),lockExpireTime,TimeUnit.MILLISECONDS);
                }else{
                    logger.debug("lock is exist!！！");
                }
                if (System.currentTimeMillis()-startTime > timeout){
                    return true;
                }
                //休眠时间
                Thread.sleep(tryInterval);
            }while (template.hasKey(lock.getValue()));
        }catch (InterruptedException e){
            logger.error(e.getMessage());
            return false;
        }
        return false;
    }

    public void releaseLock(Lock lock) {
        if (!StringUtils.isEmpty(lock.getName())) {
            template.delete(lock.getName());
        }
    }

}
