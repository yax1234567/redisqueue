package com.yax.redisqueue.client;

import com.yax.redisqueue.messageModel.DelayType;
import com.yax.redisqueue.messageModel.PushModel;
import com.yax.redisqueue.messageModel.SendType;
import com.yax.redisqueue.messageModel.TimeUnit;

/**
 * @author yax
 * @create 2019-04-13 1:30
 **/
public interface Client {
     boolean pushMessage(TimeUnit timeUnit, int delayTime, Object data, String queueName,String msgId);
     boolean pushMessage(TimeUnit timeUnit, int delayTime, String pushUrl,Object data, String queueName,String msgId);

    boolean pushMessage(TimeUnit timeUnit, SendType sendType, String pushUrl, int delayTime, Object data, String queueName);
    boolean pushMessage(TimeUnit timeUnit, SendType sendType, String pushUrl, int delayTime, Object data, String queueName,String msgId);

    boolean pushMessage( SendType sendType,String pushUrl,Object data, String queueName, String expectedTime,String msgId);
    boolean pushMessage( SendType sendType, String pushUrl,Object data, String queueName,String msgId);
    boolean pushMessage(TimeUnit timeUnit, DelayType delayType, SendType sendType, String pushUrl, Integer delayTime, Object data, String queueName, String expectedTime,String msgId);
    boolean pushMessage(PushModel pushModel);
    boolean removeMsg(String msgId,TimeUnit timeUnit,Integer delayTime,SendType sendType,String expectedTime,String pushUrl, String queueName,Object data);
}
