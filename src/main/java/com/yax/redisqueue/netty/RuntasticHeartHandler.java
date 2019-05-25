package com.yax.redisqueue.netty;

import com.alibaba.fastjson.JSONObject;
import com.yax.redisqueue.constant.Constants;
import com.yax.redisqueue.messageListener.NettyQueueListenerContainer;
import com.yax.redisqueue.messageModel.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yax
 * @create 2019-04-06 16:44
 **/

public class RuntasticHeartHandler extends ChannelInboundHandlerAdapter implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    private NettyQueueListenerContainer nettyQueueListenerContainer;
    private ChannelHandlerContext ctx;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, LockCondition> conditionMap= Collections.synchronizedMap(new WeakHashMap<>());
    private Map<String,ResponseInfo> responseInfoMap=Collections.synchronizedMap(new WeakHashMap<>());
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel ch=ctx.channel();
        log.info("---------channelId------------"+ch.id().asLongText()+"--------------通道激活----------------");
        this.ctx = ctx;
        addQueueListener();
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent)evt;
            if (event.state()== IdleState.WRITER_IDLE){
                PushInfo pushInfo=new PushInfo();
                pushInfo.setLoginType(1);
                pushMsg(pushInfo);
            }
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        try {
            if(msg instanceof MessageModel) {
                nettyQueueListenerContainer.executeCallBack((MessageModel) msg);
            }else{
                ResponseInfo responseInfo= (ResponseInfo) msg;
                String msgId=responseInfo.getMsgId();
                LockCondition lockCondition= conditionMap.get(msgId);
                if(lockCondition!=null){
                    responseInfoMap.put(msgId,responseInfo);
                    ReentrantLock lock= lockCondition.getLock();
                    Condition condition=lockCondition.getCondition();
                    try {
                        lock.lock();
                        condition.signal();
                    }finally {
                        lock.unlock();
                        conditionMap.remove(msgId);
                    }
                }else{
                    log.info("休眠线程不存在!!! msg："+msgId);
                }
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        nettyQueueListenerContainer= applicationContext.getBean(NettyQueueListenerContainer.class);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive())ctx.close();
    }

    public void addQueueListener(){
        Set<String> set= nettyQueueListenerContainer.getQueueNames();
        PushInfo pushInfo=new PushInfo();
        pushInfo.setLoginType(0);
        pushInfo.setQueueName(new ArrayList<>(set));
        pushMsg(pushInfo);
    }
    public void pushMsg(Object msg){
        String json= JSONObject.toJSONString(msg)+"\n\r";
        byte[] req = json.getBytes();
        ByteBuf m = Unpooled.buffer(req.length);
        m.writeBytes(req);
        ctx.writeAndFlush(m);
    }
    public boolean pushMsgSyn(PushModel pushModel) {
        String msgId= pushModel.getMsgId();
        ReentrantLock lock=new ReentrantLock();
        Condition condition =lock.newCondition();
        lock.lock();//请求锁
        try{
            conditionMap.put(msgId,new LockCondition(lock,condition));
            pushMsg(pushModel);
            condition.await(Constants.TIME_OUT,TimeUnit.SECONDS);//设置当前线程进入等待
        }catch (InterruptedException e) {
                    e.printStackTrace();
        }finally{
            lock.unlock();//释放锁
            ResponseInfo responseInfo= responseInfoMap.get(msgId);
            conditionMap.remove(msgId);
            responseInfoMap.remove(msgId);
            return responseInfo==null?false:responseInfo.isSuccess();
        }

    }
}
