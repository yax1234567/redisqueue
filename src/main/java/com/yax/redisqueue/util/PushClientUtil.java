package com.yax.redisqueue.util;

import com.yax.redisqueue.messageModel.DelayType;
import com.yax.redisqueue.messageModel.PushModel;
import com.yax.redisqueue.messageModel.SendType;
import com.yax.redisqueue.messageModel.TimeUnit;

/**
 * @author yax
 * @create 2019-04-11 9:32
 **/
public class PushClientUtil {
    public static PushModel buildPushModel(TimeUnit timeUnit, DelayType delayType, SendType sendType, String pushUrl, Integer delayTime, Object data, String queueName, String expectedTime,String msgId){
        int timeunit=timeUnit!=null?timeUnit.getTimeUnit():0;
        int delaytype=delayType!=null?delayType.getDelayType():0;
        int sendtype=sendType!=null?sendType.getSendType():0;
        return new PushModel(timeunit,delaytype,sendtype,pushUrl,delayTime,data,queueName,expectedTime,msgId);
    }
    public static PushModel buildRemovePushModel(String msgId,TimeUnit timeUnit,Integer delayTime,SendType sendType,String expectedTime,String pushUrl, String queueName, Object data){
        PushModel pushModel=new PushModel();
        int timeunit=timeUnit!=null?timeUnit.getTimeUnit():0;
        int sendtype=sendType!=null?sendType.getSendType():0;
        pushModel.setMsgId(msgId);
        pushModel.setQueueName(queueName);
        pushModel.setData(data);
        pushModel.setTimeUnit(timeunit);
        pushModel.setDelayTime(delayTime);
        pushModel.setSendType(sendtype);
        pushModel.setExpectedTime(expectedTime);
        pushModel.setPushUrl(pushUrl);
        pushModel.setDelayType(2);
        return pushModel;
    }
}
