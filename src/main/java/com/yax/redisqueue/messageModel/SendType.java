package com.yax.redisqueue.messageModel;

/**
 * @author yax
 * @create 2019-03-29 22:42
 **/
public enum SendType {
    BLOCK_QUEUE(0),ACTIVE_PUSH(1),TCP_PUSH(2);
    private int sendType;
    private SendType( int sendType){
        this.sendType=sendType;
    }
    public int getSendType(){
        return sendType;
    }
}
