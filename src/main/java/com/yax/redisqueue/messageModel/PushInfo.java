package com.yax.redisqueue.messageModel;

import java.util.List;

/**
 * @author yax
 * @create 2019-04-06 14:14
 **/
public class PushInfo {
    //登录信息 0 监听队列 1 心跳消息
    private int loginType;
    private List<String>  queueName;

    public PushInfo() {
    }

    public PushInfo(int loginType, List<String> queueName) {
        this.loginType = loginType;
        this.queueName = queueName;
    }


    public int getLoginType() {
        return loginType;
    }

    public void setLoginType(int loginType) {
        this.loginType = loginType;
    }

    public List<String> getQueueName() {
        return queueName;
    }

    public void setQueueName(List<String> queueName) {
        this.queueName = queueName;
    }
}
