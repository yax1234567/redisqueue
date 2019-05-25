package com.yax.redisqueue.messageListener;

import com.yax.redisqueue.client.Client;
import com.yax.redisqueue.messageModel.MessageModel;
import com.yax.redisqueue.serializer.FastJsonRedisSerializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author yax
 * @create 2019-04-05 23:31
 **/
public abstract class RedisTemplateInitializingBean implements InitializingBean {
    private RedisTemplate redisTemplate;
    private RedisConnectionFactory redisConnectionFactory;
    private MessageExceptionHandler messageExceptionHandler;
    private Client client;
    private long timeOut=50L;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public void setRetryMaxCount(int retryMaxCount){
        MessageModel.setRetryMaxCount(retryMaxCount);
    }

    public void setMessageExceptionHandler(MessageExceptionHandler messageExceptionHandler) {
        this.messageExceptionHandler = messageExceptionHandler;
    }

    public MessageExceptionHandler getMessageExceptionHandler() {
        return messageExceptionHandler;
    }

    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }
    public void setPushConfig(Client client, MessageExceptionHandler messageExceptionHandler){
        setMessageExceptionHandler(messageExceptionHandler);
        setClient(client);
    }

    public void afterPropertiesSet() throws Exception{
        if(redisTemplate==null) {
            redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            RedisSerializer stringSerializer = new StringRedisSerializer();
            RedisSerializer fastJsonSerializer = new FastJsonRedisSerializer<>(Object.class);
            redisTemplate.setKeySerializer(stringSerializer);
            redisTemplate.setValueSerializer(fastJsonSerializer);
            redisTemplate.setHashKeySerializer(stringSerializer);
            redisTemplate.setHashValueSerializer(fastJsonSerializer);
            redisTemplate.afterPropertiesSet();
        }
    }
}
