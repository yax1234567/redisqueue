package com.yax.redisqueue.messageModel;

/**
 * @author yax
 * @create 2019-03-29 22:38
 **/
public enum DelayType {
    IMMEDIATELY_PUSH(0),DELAY_PUSH(1);
    private int delayType;
    private DelayType(int delayType){
        this.delayType=delayType;
    }
    public int getDelayType(){
        return delayType;
    }
    }
