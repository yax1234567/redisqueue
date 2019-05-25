package com.yax.redisqueue.messageModel;

/**
 * @author yax
 * @create 2019-04-11 20:05
 **/
public class MessageModel {
    private String queueName;
    private Object data;
    private int retryCount;
    private static int retryMaxCount=5;

    public static void setRetryMaxCount(int retryMaxCount) {
        MessageModel.retryMaxCount = retryMaxCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public MessageModel() {
    }

    public MessageModel(String queueName, Object data) {
        this.queueName = queueName;
        this.data = data;
        this.retryCount=0;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void selfIncrement(){
      ++retryCount;
    }
    public boolean isRetry(){
        if(retryCount<retryMaxCount){
            return true;
        }
        return false;
    }
}
