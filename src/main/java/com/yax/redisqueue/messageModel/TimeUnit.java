package com.yax.redisqueue.messageModel;

/**
 * @author yax
 * @create 2019-03-29 22:48
 **/
public enum TimeUnit {
    MINUTES(0),HOURS(1),DAYS(2);
    private int time;
    private TimeUnit(int time){
        this.time=time;
    }
    public int getTimeUnit(){
        return time;
    }
}
