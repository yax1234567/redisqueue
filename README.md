使用方法
将项目的所有pom 依赖包删除 然后打包成jar
导入spring boot项目
在项目中新建spring 配置类 QueueConfig
内容如下
@Configuration
public class QueneConfig {
    //启动nettyClient客户端
    @Bean
    public NettyClient nettyClient(){
        return new NettyClient();
    }
    //注入心跳检测处理器
    @Bean
    @Scope("prototype")
    public RuntasticHeartHandler runtasticHeartHandler(){
        return new RuntasticHeartHandler();
    }

    /**
     * 注入netty 推送客户端
     * @return
     */
    @Bean
    public PushNettyClient pushNettyClient(){
        return new PushNettyClient();
    }
    /**
     * 消息监听器适配器，绑定消息处理器，利用反射技术调用消息处理器的业务方法
     * @param
     * @return
     */
    @Bean
    public QueueListenerAdapter  listenerAdapter(MessageReceiver messageReceiver){  //MessageReceiver 你自定义响应队列消息的类(该类必须注入ioc容器)  refundFailure 为响应后处理消息的方法
        QueueListenerAdapter queueListenerAdapter=new QueueListenerAdapter(messageReceiver,"refundFailure");
        return queueListenerAdapter;
    }


    @Bean
    public NettyQueueListenerContainer container(QueueListenerAdapter listenerAdapter,
                                                 PushNettyClient pushNettyClient){
        NettyQueueListenerContainer nettyQueueListenerContainer=new NettyQueueListenerContainer();
        nettyQueueListenerContainer.setLinkedBlockingQueueMode();
        nettyQueueListenerContainer.setPushConfig(pushNettyClient,null);
        nettyQueueListenerContainer.addMessageListener(listenerAdapter,"refundFailedTimedTask");  //refundFailedTimedTask 为监听的队列名 (默认处理消息推送的工作线程为2个)
        return nettyQueueListenerContainer;
    }
}

在spring 配置文件里添加
netty.client.ip=47.111.105.201 //你部署延迟队列的ip地址
netty.client.port=8081  //延迟队列端口号
