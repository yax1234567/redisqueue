package com.yax.redisqueue.messageListener;

import com.alibaba.fastjson.JSONObject;
import com.yax.redisqueue.util.ReflexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author yax
 * @create 2019-04-11 20:12
 **/
public abstract class ListenerAdapter {
    private Object delegate;
    private Method method;
    private Integer queueNameIndex;
    private Class targetClass;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public Object getDelegate() {
        return delegate;
    }

    public void setDelegate(Object delegate) {
        this.delegate = delegate;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Integer getQueueNameIndex() {
        return queueNameIndex;
    }

    public void setQueueNameIndex(Integer queueNameIndex) {
        this.queueNameIndex = queueNameIndex;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void setDefaultListenerMethod(String defaultListenerMethod){
        Class clz=delegate.getClass();
        try {
            method= ReflexUtil.getMethodByMethodName(clz,defaultListenerMethod);
            Class[] paramTypes=method.getParameterTypes();
            ParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
            String[] parameterNames=discoverer.getParameterNames(method);
            for(int i=0;i<parameterNames.length;i++){
                if("queueName".equals(parameterNames[i])){
                    queueNameIndex=i;
                }else{
                    targetClass=paramTypes[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleMessage(String queueName,Object data) throws Throwable {
        try {
            if(targetClass!=Object.class) {
                data = JSONObject.parseObject(JSONObject.toJSONString(data), targetClass);
            }
            if(queueNameIndex!=null){
                Object[] params=new Object[2];
                params[queueNameIndex]=queueName;
                if(queueNameIndex==0){
                    params[1]=data;
                }
                if(queueNameIndex==1){
                    params[0]=data;
                }

                    method.invoke(delegate,params);

            }else {
                method.invoke(delegate,data);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();// 获取目标异常
            throw t;
        }
    }
}
