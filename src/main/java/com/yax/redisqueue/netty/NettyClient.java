package com.yax.redisqueue.netty;

import com.yax.redisqueue.CustomEncoder.ByteToStringDecode;
import com.yax.redisqueue.CustomEncoder.StringToByteEncode;
import com.yax.redisqueue.client.PushNettyClient;
import com.yax.redisqueue.constant.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author yax
 * @create 2019-04-06 16:26
 **/
public class NettyClient implements EnvironmentAware, InitializingBean, ApplicationContextAware, DisposableBean {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static volatile int reconnectFailureCounter=1;//重连失败计数器
    private Environment environment;
    private ApplicationContext applicationContext;
    private  ExecutorService startGroup= Executors.newSingleThreadExecutor();
    private PushNettyClient pushNettyClient;
    public void startClient(){
        startGroup.execute(()-> connect());

    }
    public void setTimeOut(long timeOut){
        Constants.TIME_OUT=timeOut;
    }
    /**
     * 连接服务器
     */
    private void connect(){
        EventLoopGroup workerGroup=null;
        try {
        String host=environment.getProperty("netty.client.ip");
        String maxLength=environment.getProperty("netty.client.maxLength");
        int port=Integer.valueOf(environment.getProperty("netty.client.port"));
        String workGroupThreadCount=environment.getProperty("netty.client.workGroupThreadCount");
        if(workGroupThreadCount==null){
            workerGroup = new NioEventLoopGroup();
        }else{
            workerGroup=new NioEventLoopGroup(Integer.valueOf(workGroupThreadCount));
        }
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    //心跳检测
                    ch.pipeline().addLast(new IdleStateHandler(0,100,0, TimeUnit.SECONDS));
                    //通过在数据包里末尾添加换行符来防止粘包和拆包
                    ch.pipeline().addLast(new LineBasedFrameDecoder(maxLength==null?1024:Integer.valueOf(maxLength)));
                    ch.pipeline().addLast(new ByteToStringDecode());
                    ch.pipeline().addLast(new StringToByteEncode());
                    RuntasticHeartHandler runtasticHeartHandler= applicationContext.getBean(RuntasticHeartHandler.class);
                    if(pushNettyClient!=null) {
                        pushNettyClient.setRuntasticHeartHandler(runtasticHeartHandler);
                    }
                    ch.pipeline().addLast(runtasticHeartHandler);
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.addListener((future)->{
                        if(future.isSuccess()){
                            log.info("与消息队列建立连接成功");
                        }else{
                            log.info("与消息队列建立连接失败");
                        }
                    }
            );
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(workerGroup!=null) {
                workerGroup.shutdownGracefully();
            }
            if(reconnectFailureCounter<=3) {
                reConnectServer();
            }else{
                try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                reconnectFailureCounter=1;
                reConnectServer();
            }
        }
    }

    /**
     * 失败重连
     */
    private void reConnectServer(){
        try {
            Thread.sleep(5000);
            log.info("客户端进行第"+reconnectFailureCounter+"次断线重连");
            reconnectFailureCounter++;
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment=environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        pushNettyClient=applicationContext.getBean(PushNettyClient.class);
        startClient();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        startGroup.shutdownNow();
    }
}
