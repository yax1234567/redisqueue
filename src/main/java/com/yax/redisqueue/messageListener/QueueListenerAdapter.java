package com.yax.redisqueue.messageListener;



/**
 * ${DESCRIPTION}
 *
 * @author yax
 * @create 2019-03-28 10:02
 **/
public class QueueListenerAdapter extends ListenerAdapter implements MessageListener {
    public QueueListenerAdapter(Object delegate, String defaultListenerMethod) {
        super.setDelegate(delegate);
        super.setDefaultListenerMethod(defaultListenerMethod);
    }
    @Override
    public void onMessage(String queueName,Object data) throws Throwable {
        handleMessage(queueName,data);
    }
}
