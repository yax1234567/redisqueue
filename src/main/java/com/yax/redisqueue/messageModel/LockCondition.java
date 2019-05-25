package com.yax.redisqueue.messageModel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yax
 * @create 2019-05-17 17:04
 **/
public class LockCondition {
    private ReentrantLock lock;
    private Condition condition;

    public LockCondition(ReentrantLock lock, Condition condition) {
        this.lock = lock;
        this.condition = condition;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
