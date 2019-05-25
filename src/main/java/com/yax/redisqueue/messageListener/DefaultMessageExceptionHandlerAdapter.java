package com.yax.redisqueue.messageListener;

import com.yax.redisqueue.messageModel.MessageModel;

/**
 *  默认消息接收异常处理器
 * @author yax
 * @create 2019-04-12 16:34
 **/
public class DefaultMessageExceptionHandlerAdapter implements MessageExceptionHandler {
    @Override
    public void exceptionHandler( BlockQueue blockQueue,MessageModel messageModel, Throwable e) {
        if(messageModel.isRetry()){
            blockQueue.add(messageModel);
        }
    }

}
