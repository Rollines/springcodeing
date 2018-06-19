####基于 Redis 的分布式锁

基本命令主要有：

SETNX(SET If Not Exists)：当且仅当 Key 不存在时，则可以设置，否则不做任何动作。
SETEX：可以设置超时时间
其原理为：通过 SETNX 设置 Key-Value 来获得锁，随即进入死循环，每次循环判断，
如果存在 Key 则继续循环，如果不存在 Key，则跳出循环，当前任务执行完成后，
删除 Key 以释放锁


##### 启动 Application.java，连续访问两次浏览器：http://localhost:8080/index，控制台可以发现先打印了一次“执行方法”，
说明后面一个线程被锁住了，5秒后又再次打印了“执行方法”，说明锁被成功释放

#####通过这种方式创建的分布式锁存在以下问题：

高并发的情况下，如果两个线程同时进入循环，可能导致加锁失败。
SETNX 是一个耗时操作，因为它需要判断 Key 是否存在，因为会存在性能问题。


#####Redlock 是 Redis 官方推荐的一种方案，因此可靠性比较高。



####基于数据库的分布式锁
#####基于数据库表
它的基本原理和 Redis 的 SETNX 类似，其实就是创建一个分布式锁表，加锁后，我们就在表增加一条记录，释放锁即把该数据删掉，具体实现，我这里就不再一一举出。

它同样存在一些问题：

没有失效时间，容易导致死锁；
依赖数据库的可用性，一旦数据库挂掉，锁就马上不可用；
这把锁只能是非阻塞的，因为数据的 insert 操作，一旦插入失败就会直接报错。没有获得锁的线程并不会进入排队队列，要想再次获得锁就要再次触发获得锁操作；
这把锁是非重入的，同一个线程在没有释放锁之前无法再次获得该锁。因为数据库中数据已经存在了。
####乐观锁
######基本原理为：
   乐观锁一般通过 version 来实现，也就是在数据库表创建一个 version 字段，每次更新成功，则 version+1，读取数据时，
   我们将 version 字段一并读出，每次更新时将会对版本号进行比较，如果一致则执行此操作，否则更新失败！
   
   
 
第12课：分布式锁
本达人课讲述的是基于 Spring Cloud 的分布式架构，那么也带来了线程安全问题，比如一个商城系统，下单过程可能由不同的微服务协作完成，在高并发的情况下如果不加锁就会有问题，而传统的加锁方式只针对单一架构，对于分布式架构是不适合的，这时就需要用到分布式锁。

实现分布式锁的方式有很多，本文结合我的实际项目和目前的技术趋势，通过实例实现几种较为流行的分布式锁方案，最后会对不同的方案进行比较。

基于 Redis 的分布式锁
利用 SETNX 和 SETEX
基本命令主要有：

SETNX(SET If Not Exists)：当且仅当 Key 不存在时，则可以设置，否则不做任何动作。
SETEX：可以设置超时时间
其原理为：通过 SETNX 设置 Key-Value 来获得锁，随即进入死循环，每次循环判断，如果存在 Key 则继续循环，如果不存在 Key，则跳出循环，当前任务执行完成后，删除 Key 以释放锁。

这种方式可能会导致死锁，为了避免这种情况，需要设置超时时间。

下面，请看具体的实现步骤。

1.创建一个 Maven 工程并在 pom.xml 加入以下依赖：

<parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        </dependency>

        <!-- 开启web-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- redis-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    </dependencies>
2.创建启动类 Application.java：

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

}
3.添加配置文件 application.yml：

server:
  port: 8080
spring:
  redis:
    host: localhost
    port: 6379
4.创建全局锁类 Lock.java：

/**
 * 全局锁，包括锁的名称
 */
public class Lock {
    private String name;
    private String value;

    public Lock(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
5.创建分布式锁类 DistributedLockHandler.java：

@Component
public class DistributedLockHandler {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockHandler.class);
    private final static long LOCK_EXPIRE = 30 * 1000L;//单个业务持有锁的时间30s，防止死锁
    private final static long LOCK_TRY_INTERVAL = 30L;//默认30ms尝试一次
    private final static long LOCK_TRY_TIMEOUT = 20 * 1000L;//默认尝试20s

    @Autowired
    private StringRedisTemplate template;

    /**
     * 尝试获取全局锁
     *
     * @param lock 锁的名称
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(Lock lock) {
        return getLock(lock, LOCK_TRY_TIMEOUT, LOCK_TRY_INTERVAL, LOCK_EXPIRE);
    }

    /**
     * 尝试获取全局锁
     *
     * @param lock    锁的名称
     * @param timeout 获取超时时间 单位ms
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(Lock lock, long timeout) {
        return getLock(lock, timeout, LOCK_TRY_INTERVAL, LOCK_EXPIRE);
    }

    /**
     * 尝试获取全局锁
     *
     * @param lock        锁的名称
     * @param timeout     获取锁的超时时间
     * @param tryInterval 多少毫秒尝试获取一次
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(Lock lock, long timeout, long tryInterval) {
        return getLock(lock, timeout, tryInterval, LOCK_EXPIRE);
    }

    /**
     * 尝试获取全局锁
     *
     * @param lock           锁的名称
     * @param timeout        获取锁的超时时间
     * @param tryInterval    多少毫秒尝试获取一次
     * @param lockExpireTime 锁的过期
     * @return true 获取成功，false获取失败
     */
    public boolean tryLock(Lock lock, long timeout, long tryInterval, long lockExpireTime) {
        return getLock(lock, timeout, tryInterval, lockExpireTime);
    }


    /**
     * 操作redis获取全局锁
     *
     * @param lock           锁的名称
     * @param timeout        获取的超时时间
     * @param tryInterval    多少ms尝试一次
     * @param lockExpireTime 获取成功后锁的过期时间
     * @return true 获取成功，false获取失败
     */
    public boolean getLock(Lock lock, long timeout, long tryInterval, long lockExpireTime) {
        try {
            if (StringUtils.isEmpty(lock.getName()) || StringUtils.isEmpty(lock.getValue())) {
                return false;
            }
            long startTime = System.currentTimeMillis();
            do{
                if (!template.hasKey(lock.getName())) {
                    ValueOperations<String, String> ops = template.opsForValue();
                    ops.set(lock.getName(), lock.getValue(), lockExpireTime, TimeUnit.MILLISECONDS);
                    return true;
                } else {//存在锁
                    logger.debug("lock is exist!！！");
                }
                if (System.currentTimeMillis() - startTime > timeout) {//尝试超过了设定值之后直接跳出循环
                    return false;
                }
                Thread.sleep(tryInterval);
            }
            while (template.hasKey(lock.getName())) ;
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return false;
        }
        return false;
    }

    /**
     * 释放锁
     */
    public void releaseLock(Lock lock) {
        if (!StringUtils.isEmpty(lock.getName())) {
            template.delete(lock.getName());
        }
    }

}
6.最后创建 HelloController 来测试分布式锁。

@RestController
public class HelloController {

    @Autowired
    private DistributedLockHandler distributedLockHandler;

    @RequestMapping("index")
    public String index(){
        Lock lock=new Lock("lynn","min");
        if(distributedLockHandler.tryLock(lock)){
            try {
                //为了演示锁的效果，这里睡眠5000毫秒
                System.out.println("执行方法");
                Thread.sleep(5000);
            }catch (Exception e){
                e.printStackTrace();
            }
            distributedLockHandler.releaseLock(lock);
        }
        return "hello world!";
    }
}
7.测试。

启动 Application.java，连续访问两次浏览器：http://localhost:8080/index，控制台可以发现先打印了一次“执行方法”，说明后面一个线程被锁住了，5秒后又再次打印了“执行方法”，说明锁被成功释放。

通过这种方式创建的分布式锁存在以下问题：

高并发的情况下，如果两个线程同时进入循环，可能导致加锁失败。
SETNX 是一个耗时操作，因为它需要判断 Key 是否存在，因为会存在性能问题。
因此，Redis 官方推荐 Redlock 来实现分布式锁。

利用 Redlock
通过 Redlock 实现分布式锁比其他算法更加可靠，继续改造上一例的代码。

1.pom.xml 增加以下依赖：

<dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson</artifactId>
            <version>3.7.0</version>
        </dependency>
2.增加以下几个类：

/**
 * 获取锁后需要处理的逻辑
 */
public interface AquiredLockWorker<T> {
    T invokeAfterLockAquire() throws Exception;
}
/**
 * 获取锁管理类
 */
public interface DistributedLocker {

    /**
     * 获取锁
     * @param resourceName  锁的名称
     * @param worker 获取锁后的处理类
     * @param <T>
     * @return 处理完具体的业务逻辑要返回的数据
     * @throws UnableToAquireLockException
     * @throws Exception
     */
    <T> T lock(String resourceName, AquiredLockWorker<T> worker) throws UnableToAquireLockException, Exception;

    <T> T lock(String resourceName, AquiredLockWorker<T> worker, int lockTime) throws UnableToAquireLockException, Exception;

}
/**
 * 异常类
 */
public class UnableToAquireLockException extends RuntimeException {

    public UnableToAquireLockException() {
    }

    public UnableToAquireLockException(String message) {
        super(message);
    }

    public UnableToAquireLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
/**
 * 获取RedissonClient连接类
 */
@Component
public class RedissonConnector {
    RedissonClient redisson;
    @PostConstruct
    public void init(){
        redisson = Redisson.create();
    }

    public RedissonClient getClient(){
        return redisson;
    }

}
@Component
public class RedisLocker  implements DistributedLocker{

    private final static String LOCKER_PREFIX = "lock:";

    @Autowired
    RedissonConnector redissonConnector;
    @Override
    public <T> T lock(String resourceName, AquiredLockWorker<T> worker) throws InterruptedException, UnableToAquireLockException, Exception {

        return lock(resourceName, worker, 100);
    }

    @Override
    public <T> T lock(String resourceName, AquiredLockWorker<T> worker, int lockTime) throws UnableToAquireLockException, Exception {
        RedissonClient redisson= redissonConnector.getClient();
        RLock lock = redisson.getLock(LOCKER_PREFIX + resourceName);
        // Wait for 100 seconds seconds and automatically unlock it after lockTime seconds
        boolean success = lock.tryLock(100, lockTime, TimeUnit.SECONDS);
        if (success) {
            try {
                return worker.invokeAfterLockAquire();
            } finally {
                lock.unlock();
            }
        }
        throw new UnableToAquireLockException();
    }
}
3.修改 HelloController：

@RestController
public class HelloController {

    @Autowired
    private DistributedLocker distributedLocker;

    @RequestMapping("index")
    public String index()throws Exception{
        distributedLocker.lock("test",new AquiredLockWorker<Object>() {

            @Override
            public Object invokeAfterLockAquire() {
                try {
                    System.out.println("执行方法！");
                    Thread.sleep(5000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

        });
        return "hello world!";
    }
}
4.按照上节的测试方法进行测试，我们发现分布式锁也生效了。

Redlock 是 Redis 官方推荐的一种方案，因此可靠性比较高。

基于数据库的分布式锁
基于数据库表
它的基本原理和 Redis 的 SETNX 类似，其实就是创建一个分布式锁表，加锁后，我们就在表增加一条记录，释放锁即把该数据删掉，具体实现，我这里就不再一一举出。

它同样存在一些问题：

没有失效时间，容易导致死锁；
依赖数据库的可用性，一旦数据库挂掉，锁就马上不可用；
这把锁只能是非阻塞的，因为数据的 insert 操作，一旦插入失败就会直接报错。没有获得锁的线程并不会进入排队队列，要想再次获得锁就要再次触发获得锁操作；
这把锁是非重入的，同一个线程在没有释放锁之前无法再次获得该锁。因为数据库中数据已经存在了。
乐观锁
基本原理为：乐观锁一般通过 version 来实现，也就是在数据库表创建一个 version 字段，每次更新成功，则 version+1，读取数据时，我们将 version 字段一并读出，每次更新时将会对版本号进行比较，如果一致则执行此操作，否则更新失败！

悲观锁（排他锁）
实现步骤见下面说明。

####基于 Zookeeper 的分布式锁
#####分布式锁实现原理
实现原理为：

建立一个节点，假如名为 lock 。节点类型为持久节点（Persistent）
每当进程需要访问共享资源时，会调用分布式锁的 lock() 或 tryLock() 方法获得锁，这个时候会在第一步创建的 lock 节点下建立相应的顺序子节点，节点类型为临时顺序节点（EPHEMERAL_SEQUENTIAL），通过组成特定的名字 name+lock+顺序号。
在建立子节点后，对 lock 下面的所有以 name 开头的子节点进行排序，判断刚刚建立的子节点顺序号是否是最小的节点，假如是最小节点，则获得该锁对资源进行访问。
假如不是该节点，就获得该节点的上一顺序节点，并监测该节点是否存在注册监听事件。同时在这里阻塞。等待监听事件的发生，获得锁控制权。
当调用完共享资源后，调用 unlock() 方法，关闭 ZooKeeper，进而可以引发监听事件，释放该锁。
实现的分布式锁是严格的按照顺序访问的并发锁。