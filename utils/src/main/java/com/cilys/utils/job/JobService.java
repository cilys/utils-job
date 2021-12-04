package com.cilys.utils.job;

import com.cilys.utils.job.core.JobPool;
import com.cilys.utils.job.core.JobRunnable;

public class JobService {
    private static JobService instance;
    public static JobService getInstance() {
        return getInstance(5);
    }
    public static JobService getInstance(int maxPoolSize) {
        if (instance == null) {
            synchronized (JobService.class) {
                if (instance == null) {
                    instance = new JobService(maxPoolSize);
                }
            }
        }
        return instance;
    }
    private int maxPoolSize;
    private JobService(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void addTask(JobRunnable runnable){
        JobPool.getInstance().addTask(runnable);
    }

    public void shutdown(){
        JobPool.getInstance().shutdown();
    }

    public void shutdownNow(){
        JobPool.getInstance().shutdownNow();
    }

    public void setWaitTime(long waitTime) {
        JobPool.getInstance().setWaitTime(waitTime);
    }

    public void setLogLevel(int logLevel){
        JobPool.getInstance().setLogLevel(logLevel);
    }

    public JobRunnable getJobRunnableInPool(String key) {
        return JobPool.getInstance().taskInPostedCache(key);
    }
}
