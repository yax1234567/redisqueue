package com.yax.redisqueue.messageListener;

import com.yax.redisqueue.client.PushClient;
import com.yax.redisqueue.messageModel.*;
import com.yax.redisqueue.util.PushClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

/**
 * ${DESCRIPTION}
 *
 * @author yax
 * @create 2019-03-28 9:23
 **/
public class RedisQueueListenerContainer extends RedisTemplateInitializingBean {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Integer fastFailure=0;

    public void setPushClient(PushClient pushClient) {
        setClient(pushClient);
    }

    public void addMessageListener(MessageListener listener, String queue) {
        addMessageListener(listener,queue,5,1);
    }
    public void addMessageListener(MessageListener listener, String queue,int workThread,int capacity){
        BlockQueue blockQueue=new BlockQueue() {
            @Override
            public boolean add(Object o) {
                try {
                    getRedisTemplate().opsForList().rightPush(queue, o);
                    return true;
                }catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public Object pool() {
                return getRedisTemplate().opsForList().leftPop(queue, getTimeOut(), TimeUnit.SECONDS);
            }

            @Override
            public boolean retryPush(com.yax.redisqueue.messageModel.TimeUnit timeUnit, MessageModel messageModel, int delayTime,String expectedTime,String msgId) {
                PushModel pushModel= PushClientUtil.buildPushModel(timeUnit, DelayType.DELAY_PUSH, SendType.BLOCK_QUEUE,null,delayTime,messageModel.getData(),messageModel.getQueueName(),expectedTime,msgId);
                pushModel.setRetryCount(messageModel.getRetryCount());
                return getClient().pushMessage(pushModel);
            }
        };
        ExecutorService listenerGroup=Executors.newSingleThreadExecutor();
        ExecutorService workerGroup = new ThreadPoolExecutor(workThread, workThread,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(capacity,true),new CustomThreadFactory(queue),new CustomRejectedExecutionHandler());
            listenerGroup.execute(() -> ((QueueListenerTemplate) (listener1, queue1) -> {
                for(;;){
                    try {
                        Object msg = blockQueue.pool();
                        if (msg != null) {
                            workerGroup.execute(() -> {
                                MessageModel messageModel= (MessageModel) msg;
                                try {
                                    messageModel.selfIncrement();
                                    Object data=messageModel.getData();
                                    listener.onMessage(queue, data);
                                } catch (Throwable e) {
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
                    } catch (Exception e) {
                        log.error(e.toString());
                        if (fastFailure > 5) {
                            break;
                        }
                        if (e instanceof RedisSystemException) {
                            fastFailure++;
                        }
                    }
                }
            }).hook(listener, queue));
    }

    public RedisQueueListenerContainer() {
    }

    public RedisQueueListenerContainer(RedisTemplate redisTemplate) {
        super.setRedisTemplate(redisTemplate);
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        super.setRedisTemplate(redisTemplate);
    }

    public RedisQueueListenerContainer(RedisConnectionFactory redisConnectionFactory) {
       super.setRedisConnectionFactory(redisConnectionFactory);
    }

    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        super.setRedisConnectionFactory(redisConnectionFactory);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

    }

    interface QueueListenerTemplate{
       void hook(MessageListener listener, String queue);
    }
}
