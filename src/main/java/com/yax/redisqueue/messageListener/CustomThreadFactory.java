package com.yax.redisqueue.messageListener;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yax
 * @create 2019-04-12 9:27
 **/
public class CustomThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    CustomThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix+"-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread( r,namePrefix + threadNumber.getAndIncrement());
        if (t.isDaemon())
            t.setDaemon(true);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
