#####eureka.server.enable-self-preservation：
     是否开启自我保护，默认为 true，在开启自我保护的情况下，注册中心在丢失客户端时，会进入自动保护模式，注册中心并不会将该服务从注册中心删除掉。这里我设置为 false，即关闭自我保护。根据我的经验，如果设置为 true，在负载均衡条件下，一个服务挂掉后，注册中心并没有删掉该服务，会导致客户端请求的时候可能会请求到该服务，导致系统无法访问，所以我推荐将这个属性设置为 false。
#####eureka.instance.preferIpAddress：
    是否以 IP 注册到注册中心，Eureka 默认是以 hostname 来注册的。
#####client.serviceUrl.defaultZone：
     注册中心默认地址。
##### Application.class 
     不能再默认的包下得在新建的包下