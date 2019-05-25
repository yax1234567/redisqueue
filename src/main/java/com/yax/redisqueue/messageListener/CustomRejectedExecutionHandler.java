package com.yax.redisqueue.messageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池拒绝策略
 * @author yax
 * @create 2019-04-12 0:36
 **/
public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            // 由blockingqueue的offer实现put阻塞方法
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
