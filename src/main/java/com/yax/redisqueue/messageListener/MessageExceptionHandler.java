package com.yax.redisqueue.messageListener;

import com.yax.redisqueue.messageModel.MessageModel;

/**
 * @author yax
 * @create 2019-04-12 16:30
 **/
public interface MessageExceptionHandler {
    void exceptionHandler(BlockQueue blockQueue,MessageModel messageModel,Throwable e);
}
