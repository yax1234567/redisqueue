package com.yax.redisqueue.messageModel;


import java.io.Serializable;

/**
 * @author yax
 * @create 2019-03-29 22:23
 **/
public class PushModel<T> implements Serializable {
    //时间单位   例 0 表示分 1表示 小时 2表示 天
    private int timeUnit;
    //延迟类型 0 代表立即推送 1代表延迟推送
    private int delayType;
    //发送方式  0 代表redis 阻塞队列   1 代表主动推送 （需要推送地址）
    private int sendType;
    //推送地址
    private String pushUrl;
    //延迟时间  例 10
    private Integer delayTime;
    //消息体
    private T data;
    //队列名
    private String queueName;
    //预期的时间  例 2019-03-11 15:26:00
    private String expectedTime;
    //重试次数
    private int retryCount;
    //消息Id
    private String msgId;
    private boolean isSyn;
    public PushModel() {
    }

    public PushModel(int timeUnit, int delayType, int sendType, String pushUrl, Integer delayTime, T data, String queueName, String expectedTime,String msgId) {
        this.timeUnit = timeUnit;
        this.delayType = delayType;
        this.sendType = sendType;
        this.pushUrl = pushUrl;
        this.delayTime = delayTime;
        this.data = data;
        this.queueName = queueName;
        this.expectedTime = expectedTime;
        this.msgId=msgId;
    }

    public boolean isSyn() {
        return isSyn;
    }

    public void setSyn(boolean syn) {
        isSyn = syn;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getDelayType() {
        return delayType;
    }

    public void setDelayType(int delayType) {
        this.delayType = delayType;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    public Integer getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Integer delayTime) {
        this.delayTime = delayTime;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExpectedTime() {
        return expectedTime;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setExpectedTime(String expectedTime) {
        this.expectedTime = expectedTime;
    }
    public void selfIncrement(){
        ++retryCount;
    }
}
