package com.redis.demo;

import com.redis.demo.lock.AquiredLockWorker;
import com.redis.demo.lock.DistributedLocker;
import com.redis.demo.zookeeper.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/19 上午11:50
 */
@RestController
public class HelloController {

//    @Autowired
//    private DistributedLockHandler distributedLockHandler;
//    @RequestMapping("index")
//    public String index() throws Exception{
//        Lock lock = new Lock("Doctor.chen","min");
//        if (distributedLockHandler.tryLock(lock)){
//            try {
//                System.out.println("执行方法====");
//                Thread.sleep(5000);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            distributedLockHandler.releaseLock(lock);
//        }
//        return "Doctor.chen welcome!";
//    }


    /**
     * 利用 Redlock
     * 通过 Redlock 实现分布式锁比其他算法更加可靠，继续改造上一例的代码。
     */

//    @Autowired
//    private DistributedLocker distributedLocker;
//    @RequestMapping("index")
//    public String index() throws Exception{
//        distributedLocker.lock("hello demo!", new AquiredLockWorker<Object>() {
//            @Override
//            public Object invokeAfterLockAquire(){
//                try {
//                    System.out.println("执行方法！");
//                    Thread.sleep(5000);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        });
//        return "hello demo!";
//    }
    /**
     * ZooKeeper 来加锁
     */
    @RequestMapping("index")
    public String index() throws Exception{
        DistributedLock lock = new DistributedLock("localhost:2181","lock");
        lock.lock();
        if (lock!=null){
            System.out.println("====执行方====");
            Thread.sleep(5000);
            lock.unlock();
        }
        return "welcome to opendworl!";
    }

}
