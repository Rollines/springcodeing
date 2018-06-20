package com.test.example.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @Author: Doctor.chen
 * @Date: 2018/6/20
 */
public class DistributedLock implements Lock,Watcher {
    private ZooKeeper zk;
    private String root = "/locks";
    //竞争资源的标志
    private String lockName;
    //等待前一个节点
    private String waitNode;
    //当前锁
    private String myZnode;
    //计算器
    private CountDownLatch latch;
    private CountDownLatch connectedSignal=new CountDownLatch(1);
    private int sessionTimeout = 30000;

    /**
     *创建分布式锁,使用前请确认config配置的zookeeper服务可用
     * @param config  localhost:2181
     * @param lockName
     */
    public DistributedLock(String config,String lockName){
        this.lockName = lockName;
        //创建一个服务器连接u
        try {
            zk = new ZooKeeper(config,sessionTimeout,this);
            //不执行 Watcher
            connectedSignal.await();
            Stat stat = zk.exists(root,false);
            if (stat == null){
                //如果stat==null那就创建ROOT节点
                zk.create(root,new byte[0],ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        }catch (IOException e){
            throw new LockException(e);
        }catch (KeeperException e) {
            throw new LockException(e);
        } catch (InterruptedException e) {
            throw new LockException(e);
        }
    }

    @Override
    public void lock() {
        if (this.tryLock()){
            System.out.println("Thread"+Thread.currentThread().getId()+" "+myZnode+"get lock true");
            return;
        }else {
            try {
                waitForLock(waitNode,sessionTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean waitForLock(String lower, long waitTime) throws InterruptedException, KeeperException {
        //同时注册监听。
        Stat stat = zk.exists(root +"/"+lower,true);
        if (stat!=null){
            System.out.println("Thread " + Thread.currentThread().getId() + " waiting for " + root + "/" + lower);
            this.latch = new CountDownLatch(1);
            this.latch.await(waitTime,TimeUnit.MILLISECONDS);
            this.latch=null;
        }
        return true;
    }

    public void lockInterruptibly() throws InterruptedException {
        this.lock();
    }

    public boolean tryLock() {
        try {
            String splitStr = "_lock_";
            if (lockName.contains(splitStr)) {
                throw new LockException("lockname can not contains \\U000B");

            }//创建子节点
                myZnode = zk.create(root + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
                System.out.println(myZnode+" is created ");

                //取出子节点
                List<String> subNodes = zk.getChildren(root,false);
                //取出所有lockName的锁
                List<String> lockObjNodes = new ArrayList<String>();
                for (String node : subNodes){
                    String _node = node.split(splitStr)[0];
                    if (_node.equals(lockName)){
                        lockObjNodes.add(node);
                    }
                }
                //集合
                Collections.sort(lockObjNodes);

                if (myZnode.equals(root+"/"+lockObjNodes.get(0))){
                    //如果是最小节点,表示取得锁
                    System.out.println(myZnode+"=="+lockObjNodes.get(0));
                    return true;
                }
                //如果不是最小节点，找到比他小的节点
                String subMyZode = myZnode.substring(myZnode.lastIndexOf("/")+1);
                //找到前一个子节点
                waitNode = lockObjNodes.get(Collections.binarySearch(lockObjNodes,subMyZode)-1);
        }catch (KeeperException e){
            throw new LockException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) {
        try {
            if(this.tryLock()){
                return true;
            }
            return waitForLock(waitNode,time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void unlock() {
        try {
            System.out.println("unlock"+myZnode);
            zk.delete(myZnode,-1);
            myZnode = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    public Condition newCondition() {
        return null;
    }

    /**
     * 监控跟节点
     * @param event
     */
    public void process(WatchedEvent event) {
        //建立连接
        if (event.getState() == Event.KeeperState.SyncConnected){
            connectedSignal.countDown();
            return;
        }
        //其他线程放弃锁的标志
        if(this.latch != null) {
            this.latch.countDown();
        }
    }

    private class LockException extends RuntimeException {
        private static final long serialVersionUID = 4538635504379200849L;

        public LockException(String e) {
            super(e);
        }
        public LockException(Exception e){
            super(e);
        }
    }
}
