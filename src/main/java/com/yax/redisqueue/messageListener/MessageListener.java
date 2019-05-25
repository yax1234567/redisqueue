package com.yax.redisqueue.messageListener;

/**
 * ${DESCRIPTION}
 *
 * @author yax
 * @create 2019-03-28 9:19
 **/
public interface MessageListener {
    void onMessage(String queueName,Object data) throws Throwable;
}
