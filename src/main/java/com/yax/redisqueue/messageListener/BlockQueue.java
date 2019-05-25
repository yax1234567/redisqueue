package com.yax.redisqueue.messageListener;


import com.yax.redisqueue.messageModel.MessageModel;
import com.yax.redisqueue.messageModel.TimeUnit;


/**
 * @author yax
 * @create 2019-04-11 20:34
 **/
public interface BlockQueue {
      boolean add(Object o);
      Object pool();
      boolean retryPush(TimeUnit timeUnit, MessageModel messageModel,int delayTime,String expectedTime,String msgId);

}
