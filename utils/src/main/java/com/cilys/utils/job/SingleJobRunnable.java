package com.cilys.utils.job;

import com.cilys.utils.job.core.JobRunnable;

public abstract class SingleJobRunnable extends JobRunnable {
    public SingleJobRunnable() {

    }

    public SingleJobRunnable(long defaultOutTime) {
        this.defaultOutTime = defaultOutTime;
    }

    public SingleJobRunnable(String name, long defaultOutTime) {
        super(name);
        this.defaultOutTime = defaultOutTime;
    }

    public SingleJobRunnable(String name, long priority, long defaultOutTime) {
        super(name, priority);
        this.defaultOutTime = defaultOutTime;
    }

    public SingleJobRunnable(long priority, long defaultOutTime) {
        super(priority);
        this.defaultOutTime = defaultOutTime;
    }

    private long defaultOutTime = 60 * 1000;

    public SingleJobRunnable setDefaultOutTime(long defaultOutTime) {
        if (defaultOutTime < 1) {
            defaultOutTime = 60 * 1000;
        }
        this.defaultOutTime = defaultOutTime;
        return this;
    }

    public boolean isOutTime(){
        return System.currentTimeMillis() - getTime() >= defaultOutTime;
    }
}
