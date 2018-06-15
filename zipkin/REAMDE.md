####Sleuth 术语
######span（跨度）：
     基本工作单元。例如，在一个新建的 span 中发送一个 RPC 等同于发送一个回应请求给 RPC，span 通过一个64位 ID 唯一标识，trace 以另一个64位 ID 表示，span 还有其他数据信息，比如摘要、时间戳事件、关键值注释（tags）、span 的 ID，以及进度 ID（通常是 IP 地址)。span 在不断的启动和停止，同时记录了时间信息，当你创建了一个 span，你必须在未来的某个时刻停止它。
######trace（追踪）：
     一组共享“root span”的 span 组成的树状结构成为 trace。trace 也用一个64位的 ID 唯一标识，trace中的所有 span 都共享该 trace 的 ID。
######annotation（标注）：
     用来及时记录一个事件的存在，一些核心 annotations 用来定义一个请求的开始和结束。
######cs，即 Client Sent，
     客户端发起一个请求，这个 annotion 描述了这个 span 的开始。
######sr，即 Server Received，
     服务端获得请求并准备开始处理它，如果将其 sr 减去 cs 时间戳便可得到网络延迟。
######ss，即 Server Sent，
     注解表明请求处理的完成（当请求返回客户端），如果 ss 减去 sr 时间戳便可得到服务端需要的处理请求时间。
######cr，即 Client Received，
    表明 span 的结束，客户端成功接收到服务端的回复，如果 cr 减去 cs 时间戳便可得到客户端从服务端获取回复的所有所需时间。
    
    
``