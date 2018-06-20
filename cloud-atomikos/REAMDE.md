#### SpringBoot 集成 Atomikos 实现分布式事务
Atomikos 简介
#####Atomikos 是一个为 Java 平台提供增值服务的开源类事务管理器。

以下是包括在这个开源版本中的一些功能：

全面崩溃 / 重启恢复；
兼容标准的 SUN 公司 JTA API；
嵌套事务；
为 XA 和非 XA 提供内置的 JDBC 适配器。
注释：XA 协议由 Tuxedo 首先提出的，并交给 X/Open 组织，作为资源管理器（数据库）与事务管理器的接口标准。目前，Oracle、Informix、DB2 和 Sybase 等各大数据库厂家都提供对 XA 的支持。XA 协议采用两阶段提交方式来管理分布式事务。XA 接口提供资源管理器与事务管理器之间进行通信的标准接口。XA 协议包括两套函数，以 xa_ 开头的及以 ax_ 开头的。