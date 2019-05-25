package com.yax.redisqueue.client;

import com.yax.redisqueue.messageModel.DelayType;
import com.yax.redisqueue.messageModel.PushModel;
import com.yax.redisqueue.messageModel.SendType;
import com.yax.redisqueue.messageModel.TimeUnit;
import com.yax.redisqueue.netty.RuntasticHeartHandler;
import com.yax.redisqueue.util.IdGen;
import com.yax.redisqueue.util.PushClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


/**
 * @author yax
 * @create 2019-04-08 14:58
 **/
public class PushNettyClient implements Client  {
    private static Logger log = LoggerFactory.getLogger(PushNettyClient.class);
    private RuntasticHeartHandler runtasticHeartHandler;
    public void setRuntasticHeartHandler(RuntasticHeartHandler runtasticHeartHandler) {
        this.runtasticHeartHandler = runtasticHeartHandler;
    }

    public RuntasticHeartHandler getRuntasticHeartHandler() {
        return runtasticHeartHandler;
    }
    /**
     *  延迟阻塞队列推送
     * @param timeUnit
     * @param delayTime
     * @param data
     * @param queueName
     * @return
     */
    public boolean pushMessage(TimeUnit timeUnit, int delayTime, Object data, String queueName,String msgId){
        return pushMessage(timeUnit, DelayType.DELAY_PUSH, SendType.BLOCK_QUEUE,null,delayTime,data,queueName,null,msgId);
    }

    public boolean pushMessage(TimeUnit timeUnit, SendType sendType, String pushUrl, int delayTime, Object data, String queueName){
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,sendType,pushUrl,delayTime,data,queueName,null,null);
    }
    /**
     * 延迟推送 主动推送
     * @param timeUnit
     * @param delayTime
     * @param pushUrl
     * @param data
     * @param queueName
     * @return
     */
    public boolean pushMessage(TimeUnit timeUnit, int delayTime, String pushUrl,Object data, String queueName,String msgId){
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,SendType.ACTIVE_PUSH,pushUrl,delayTime,data,queueName,null,msgId);
    }

    public boolean pushMessageByNetty(TimeUnit timeUnit, int delayTime, Object data, String queueName){
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,delayTime,data,queueName,null,null);
    }
    public boolean pushMessageByNettySyn(TimeUnit timeUnit, int delayTime, Object data, String queueName){
        return pushMessageSyn(timeUnit,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,delayTime,data,queueName,null,null);
    }

    public boolean pushMessageByNetty(TimeUnit timeUnit, int delayTime, Object data, String queueName,String msgId){
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,delayTime,data,queueName,null,msgId);
    }
    public boolean pushMessageByNettySyn(TimeUnit timeUnit, int delayTime, Object data, String queueName,String msgId){
        return pushMessageSyn(timeUnit,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,delayTime,data,queueName,null,msgId);
    }

    public boolean pushMessageByNetty( String expectedTime, Object data, String queueName){
        return pushMessage(null,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,null,data,queueName,expectedTime,null);
    }
    public boolean pushMessageByNettySyn( String expectedTime, Object data, String queueName){
        return pushMessageSyn(null,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,null,data,queueName,expectedTime,null);
    }

    public boolean pushMessageByNetty( String expectedTime, Object data, String queueName,String msgId){
        return pushMessage(null,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,null,data,queueName,expectedTime,msgId);
    }
    public boolean pushMessageByNettySyn( String expectedTime, Object data, String queueName,String msgId){
        return pushMessageSyn(null,DelayType.DELAY_PUSH,SendType.TCP_PUSH,null,null,data,queueName,expectedTime,msgId);
    }

    public boolean pushMessageByNettyImmediately( Object data, String queueName){
        return pushMessage(null,DelayType.IMMEDIATELY_PUSH,SendType.TCP_PUSH,null,null,data,queueName,null,null);
    }
    public boolean pushMessageByNettyImmediatelySyn( Object data, String queueName){
        return pushMessageSyn(null,DelayType.IMMEDIATELY_PUSH,SendType.TCP_PUSH,null,null,data,queueName,null,null);
    }
    /**
     * 延迟推送
     * @param timeUnit
     * @param sendType
     * @param delayTime
     * @param data
     * @param queueName
     * @return
     */
    public boolean pushMessage(TimeUnit timeUnit,SendType sendType,String pushUrl, int delayTime, Object data, String queueName,String msgId){
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,sendType,pushUrl,delayTime,data,queueName,null,msgId);
    }


    /**
     * 预期时间推送
     * @param sendType
     * @param pushUrl
     * @param data
     * @param queueName
     * @param expectedTime
     * @return
     */
    public boolean pushMessage( SendType sendType,String pushUrl,Object data, String queueName, String expectedTime,String msgId){
        return pushMessage(null,null,sendType,pushUrl,0,data,queueName,expectedTime,msgId);
    }

    /**
     * 立即推送
     * @param sendType
     * @param pushUrl
     * @param data
     * @param queueName
     * @return
     */
    public boolean pushMessage( SendType sendType, String pushUrl,Object data, String queueName,String msgId){
        return pushMessage(null,DelayType.IMMEDIATELY_PUSH,sendType,pushUrl,0,data,queueName,null,msgId);
    }

    public boolean pushMessage(TimeUnit timeUnit, DelayType delayType, SendType sendType, String pushUrl, Integer delayTime, Object data, String queueName, String expectedTime,String msgId) {

         return pushMessage(PushClientUtil.buildPushModel(timeUnit,delayType,sendType,pushUrl,delayTime,data,queueName,expectedTime,msgId));


    }
    public boolean pushMessageSyn(TimeUnit timeUnit, DelayType delayType, SendType sendType, String pushUrl, Integer delayTime, Object data, String queueName, String expectedTime,String msgId) {

        return pushMessageSyn(PushClientUtil.buildPushModel(timeUnit,delayType,sendType,pushUrl,delayTime,data,queueName,expectedTime,msgId));

    }
    public boolean pushMessage(PushModel pushModel) {
        try {
            runtasticHeartHandler.pushMsg(pushModel);
            return true;
        }catch (Exception e){
           log.error(e.getMessage());
            return false;
        }

    }
    public boolean pushMessageSyn(PushModel pushModel) {
        pushModel.setSyn(true);
        if(StringUtils.isEmpty(pushModel.getMsgId())) {
            pushModel.setMsgId(String.valueOf(IdGen.get().nextId()));
        }
        return runtasticHeartHandler.pushMsgSyn(pushModel);
    }

    @Override
    public boolean removeMsg(String msgId,TimeUnit timeUnit,Integer delayTime,SendType sendType,String expectedTime,String pushUrl, String queueName, Object data) {
        try {
            PushModel pushModel=PushClientUtil.buildRemovePushModel(msgId, timeUnit, delayTime, sendType, expectedTime, pushUrl, queueName, data);
            runtasticHeartHandler.pushMsg(pushModel);
            return true;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }
    public boolean removeMsgSyn(String msgId,TimeUnit timeUnit,Integer delayTime,SendType sendType,String expectedTime,String pushUrl, String queueName, Object data) {
        PushModel pushModel=PushClientUtil.buildRemovePushModel(msgId, timeUnit, delayTime, sendType, expectedTime, pushUrl, queueName, data);
        pushModel.setSyn(true);
        pushModel.setMsgId(String.valueOf(IdGen.get().nextId()));
         return  runtasticHeartHandler.pushMsgSyn(pushModel);

    }
}
