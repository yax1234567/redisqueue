package com.yax.redisqueue.client;


import com.alibaba.fastjson.JSONObject;
import com.yax.redisqueue.messageListener.RedisTemplateInitializingBean;
import com.yax.redisqueue.messageModel.*;
import com.yax.redisqueue.util.HttpClient;
import com.yax.redisqueue.util.OkHttpClient;
import com.yax.redisqueue.util.PushClientUtil;
import com.yax.redisqueue.util.VeDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.Date;


/**
 * @author yax
 * @create 2019-03-29 22:30
 **/
public class PushClient  extends RedisTemplateInitializingBean implements Client{
    private static Logger log = LoggerFactory.getLogger(PushClient.class);
    private  String messageQueueUrl;
    private  PushMethod pushMethod=PushMethod.REDIS;
    private HttpClient httpClient=new OkHttpClient();
    public PushClient(RedisTemplate redisTemplate){
        super.setRedisTemplate(redisTemplate);
    }

    public PushClient(RedisConnectionFactory redisConnectionFactory) {
       super.setRedisConnectionFactory(redisConnectionFactory);
    }

    public PushClient(String messageQueueUrl){
        this.messageQueueUrl=messageQueueUrl;
        this.pushMethod=PushMethod.HTTP;
    }
    public PushClient(String messageQueueUrl,HttpClient httpClient) {
        setPushConfig(messageQueueUrl,PushMethod.HTTP,httpClient,null);
    }
    public void setPushConfig(String messageQueueUrl, PushMethod pushMethod,HttpClient httpClient,RedisTemplate redisTemplate) {
        super.setRedisTemplate(redisTemplate);
        this.pushMethod=pushMethod;
        this.messageQueueUrl=messageQueueUrl;
        this.httpClient=httpClient;
    }

    public void setRedisConnectionFactory(RedisConnectionFactory redisConnectionFactory) {
        super.setRedisConnectionFactory(redisConnectionFactory);
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
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,SendType.BLOCK_QUEUE,null,delayTime,data,queueName,null,msgId);
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


    public boolean pushMessage(TimeUnit timeUnit, SendType sendType, String pushUrl, int delayTime, Object data, String queueName){
        return pushMessage(timeUnit,DelayType.DELAY_PUSH,sendType,pushUrl,delayTime,data,queueName,null,null);
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
        return pushMessage(null,null,sendType,pushUrl,0,data,queueName,expectedTime, msgId);
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

    public boolean pushMessage(TimeUnit timeUnit, DelayType delayType, SendType sendType, String pushUrl, Integer delayTime, Object data, String queueName, String expectedTime,String msgId){
        PushModel pushModel= PushClientUtil.buildPushModel(timeUnit,delayType,sendType,pushUrl,delayTime,data,queueName,expectedTime,msgId);
        return pushMessage(pushModel);
    }
    public boolean pushMessage(PushModel pushModel){
        if(pushMethod==PushMethod.REDIS){
            return pushRedis(pushModel);
        }
        if(pushMethod==PushMethod.HTTP) {
            String response = httpClient.post(messageQueueUrl, pushModel);
            ResponseModel responseModel = JSONObject.parseObject(response, ResponseModel.class);
            if (responseModel.getCode() == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeMsg(String msgId,TimeUnit timeUnit,Integer delayTime,SendType sendType,String expectedTime,String pushUrl, String queueName, Object data) {
        PushModel pushModel=PushClientUtil.buildRemovePushModel(msgId, timeUnit, delayTime, sendType, expectedTime, pushUrl, queueName, data);
        return pushMessage(pushModel);
    }


    public boolean pushRedis(PushModel requestModel){
        int delayType= requestModel.getDelayType();
        if(delayType==0){
            return push(requestModel);
        }else if(delayType==1){
            //return delayPush(requestModel);
            return delayPushZset(requestModel);
        }else{
            log.error("redis 模式下 无法删除队列消息!!!");
        }
        return false;
    }
    /**
     * 立即推送
     * @param requestModel
     * @return
     */
    private boolean push(PushModel requestModel){
        String queueName=requestModel.getQueueName();
        Object data=requestModel.getData();
        int sendType=requestModel.getSendType();
        if(sendType==0){
            return rightPush(queueName,data);
        }
        if(sendType==1){
            String pushUrl=requestModel.getPushUrl();
            String response= httpClient.post(pushUrl, data);
            if(response==null){
                return false;
            }
            ResponseModel responseModel=JSONObject.parseObject(response,ResponseModel.class);
            if(responseModel.getCode()==0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 延迟推送
     * @param requestModel
     * @return
     */
    private boolean delayPush(PushModel requestModel){
        String expectedTime=requestModel.getExpectedTime();
        long delaySeconds=0;
        if(StringUtils.isEmpty(expectedTime)){
            int timeUnit=requestModel.getTimeUnit();
            int time=requestModel.getDelayTime();
            if(timeUnit==0){
                delaySeconds=time*60L;
                long timeM=System.currentTimeMillis()+delaySeconds*1000L;
                expectedTime= VeDate.stampToDate(timeM);
            }
            if(timeUnit==1){
                delaySeconds=time*60L*60L;
                long timeM=System.currentTimeMillis()+delaySeconds*1000L;
                expectedTime= VeDate.stampToDate(timeM);
            }
            if(timeUnit==2){
                delaySeconds=time*60L*60L*24L;
                long timeM=System.currentTimeMillis()+delaySeconds*1000L;
                expectedTime= VeDate.stampToDate(timeM);
            }

        }else{
            Date date= VeDate.strToDateLongT(expectedTime);
            delaySeconds=(date.getTime()-System.currentTimeMillis())/1000;
        }
        delaySeconds=delaySeconds+60L*60L;
        return rightPush(expectedTime,requestModel,delaySeconds);
    }
    /**
     * 延迟推送 (zset版本)
     * @param requestModel
     * @return
     */
    public boolean delayPushZset(PushModel requestModel){
        String expectedTime=requestModel.getExpectedTime();
        long timeStamp=0;
        long delaySeconds;
        if(StringUtils.isEmpty(expectedTime)){
            int timeUnit=requestModel.getTimeUnit();
            int time=requestModel.getDelayTime();
            if(timeUnit==0){
                delaySeconds=time*60L;
                timeStamp=System.currentTimeMillis()+delaySeconds*1000L;
            }
            if(timeUnit==1){
                delaySeconds=time*60L*60L;
                timeStamp=System.currentTimeMillis()+delaySeconds*1000L;
            }
            if(timeUnit==2){
                delaySeconds=time*60L*60L*24L;
                timeStamp=System.currentTimeMillis()+delaySeconds*1000L;
            }
        }else{
            Date date= VeDate.strToDateLongT(expectedTime);
            timeStamp=date.getTime();
        }
        return zaddSet("redisDelayQueue",requestModel,timeStamp);
    }
    /**
     * 新增一个有序集合
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean zaddSet(String key,Object value,double score){
        try {
            return getRedisTemplate().opsForZSet().add(key, value, score);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将值放入队列右边
     *
     * @param key   键
     * @param value 值
     * @return
     */
    private boolean rightPush(String key, Object value) {
        try {
            getRedisTemplate().opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean rightPush(String key, Object value, long time) {
        try {
            getRedisTemplate().opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    private boolean expire(String key, long time) {
        try {
            if (time > 0) {
                getRedisTemplate().expire(key, time, java.util.concurrent.TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
