package com.yax.redisqueue.messageListener;

import com.yax.redisqueue.client.PushNettyClient;
import com.yax.redisqueue.messageModel.*;
import com.yax.redisqueue.util.PushClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yax
 * @create 2019-04-08 13:09
 **/
public class NettyQueueListenerContainer extends RedisTemplateInitializingBean {
    private boolean isRedisQueue=false;
    private Integer fastFailure=0;
    private BlockQueue blockQueue;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, WorkGroupListener> nettyMap =new ConcurrentHashMap<>();

    private  LinkedBlockingQueue linkedBlockingQueue;
    public Set<String> getQueueNames(){
        return nettyMap.keySet();
    }

    public void setPushNettyClient(PushNettyClient pushNettyClient) {
        setClient(pushNettyClient);
    }


    public void addMessageListener(MessageListener listener, String queue){
        addMessageListener(listener,queue,2,1);
    }
    public void addMessageListener(MessageListener listener, String queue,int workThread,int capacity){
        addNettyMap(queue,workThread,listener,capacity);
    }
    public void setLinkedBlockingQueueMode(){
        linkedBlockingQueue= new LinkedBlockingQueue();
        blockQueue=new BlockQueue() {
            @Override
            public boolean add(Object o) {
                return linkedBlockingQueue.add(o);
            }

            @Override
            public Object pool() {
                return linkedBlockingQueue.poll();
            }

            @Override
            public boolean retryPush(com.yax.redisqueue.messageModel.TimeUnit timeUnit, MessageModel messageModel,int delayTime,String expectedTime,String msgId){
                return doRetryPush(timeUnit, messageModel, delayTime,expectedTime,msgId);
            }
        };
    }
    public void SetRedisBlockQueueMode(RedisTemplate redisTemplate){
        setRedisTemplate(redisTemplate);
        doSetRedisBlockQueueMode();
    }
    public void SetRedisBlockQueueMode(RedisConnectionFactory redisConnectionFactory){
        setRedisConnectionFactory(redisConnectionFactory);
        doSetRedisBlockQueueMode();
    }
    private void doSetRedisBlockQueueMode(){
        this.isRedisQueue=true;
        blockQueue=new BlockQueue() {
            @Override
            public boolean add(Object o) {
                try {
                    getRedisTemplate().opsForList().rightPush("nettyBlockQueue", o);
                    return true;
                }catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public Object pool() {
                return getRedisTemplate().opsForList().leftPop("nettyBlockQueue",getTimeOut(), TimeUnit.SECONDS);
            }

            @Override
            public boolean retryPush(com.yax.redisqueue.messageModel.TimeUnit timeUnit, MessageModel messageModel,int delayTime,String expectedTime,String msgId){
                return doRetryPush(timeUnit, messageModel, delayTime,expectedTime,msgId);
            }
        };
    }

    private void addNettyMap(String queue,int workThread,MessageListener messageListener,int capacity){
        ExecutorService workerGroup = new ThreadPoolExecutor(workThread, workThread,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(capacity,true),new CustomThreadFactory(queue) ,new CustomRejectedExecutionHandler());
        WorkGroupListener workGroupListener=new WorkGroupListener(messageListener,workerGroup);
        nettyMap.put(queue,workGroupListener);
    }
    public void executeCallBack(MessageModel msg){
        blockQueue.add(msg);
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        if(isRedisQueue) {
            super.afterPropertiesSet();
        }
        ExecutorService listenerGroup= Executors.newSingleThreadExecutor();
        listenerGroup.execute(() -> {
            for(;;) {
                try {
                    Object msg = blockQueue.pool();
                    if (msg != null) {
                        MessageModel messageModel = (MessageModel) msg;
                        Object data = messageModel.getData();
                        String queueName = messageModel.getQueueName();
                        WorkGroupListener workGroupListener = getWorkGroupListenerByQueueName(queueName);
                        MessageListener messageListener = workGroupListener.getMessageListener();
                        ExecutorService workerGroup = workGroupListener.getExecutorService();
                        workerGroup.execute(() ->{
                            try{
                                messageModel.selfIncrement();
                                messageListener.onMessage(queueName, data);
                            }catch (Throwable e){
                                e.printStackTrace();
                                MessageExceptionHandler messageExceptionHandler=getMessageExceptionHandler();
                                if(messageExceptionHandler!=null) {
                                    messageExceptionHandler.exceptionHandler(blockQueue, messageModel, e);
                                }
                            }
                        });
                    }
                    Thread.sleep(1);//防止死循环
                    fastFailure = 0;
                }catch (Exception e){
                    e.printStackTrace();
                    if (fastFailure > 5) {
                        break;
                    }
                    if (e instanceof RedisSystemException) {
                        fastFailure++;
                    }
                }
            }
        });
    }
    private WorkGroupListener getWorkGroupListenerByQueueName(String queueName){
        return nettyMap.get(queueName);
    }
    private boolean doRetryPush(com.yax.redisqueue.messageModel.TimeUnit timeUnit, MessageModel messageModel,int delayTime,String expectedTime,String msgId){
        try {
            PushModel pushModel = PushClientUtil.buildPushModel(timeUnit, DelayType.DELAY_PUSH, SendType.TCP_PUSH, null, delayTime, messageModel.getData(), messageModel.getQueueName(), expectedTime,msgId);
            pushModel.setRetryCount(messageModel.getRetryCount());
            getClient().pushMessage(pushModel);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
