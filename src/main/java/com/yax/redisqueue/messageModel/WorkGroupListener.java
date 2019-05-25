package com.yax.redisqueue.messageModel;

import com.yax.redisqueue.messageListener.MessageListener;

import java.util.concurrent.ExecutorService;

/**
 * @author yax
 * @create 2019-04-11 21:03
 **/
public class WorkGroupListener {
    private MessageListener messageListener;
    private ExecutorService executorService;

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public WorkGroupListener(MessageListener messageListener, ExecutorService executorService) {
        this.messageListener = messageListener;
        this.executorService = executorService;
    }

    public WorkGroupListener() {
    }
}
