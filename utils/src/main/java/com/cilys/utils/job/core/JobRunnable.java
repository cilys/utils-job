package com.cilys.utils.job.core;

public abstract class JobRunnable<T> implements Runnable, Comparable<JobRunnable> {
    protected boolean running = true;
    private int retryCount = 0;

    private long time;

    public void stop(){
        running = false;
        time = 0;
        JobPool.getInstance().removeTaskFromPostedCache(key());
    }

    public boolean isRunning() {
        return running;
    }

    private String name;

    private long priority = 10;

    public JobRunnable(){
        this(null);
    }

    public JobRunnable(String name) {
        this(name, 10);
    }

    public JobRunnable(String name, long priority) {
        this.name = name;
        this.priority = priority;
        time = System.currentTimeMillis();
    }


    public JobRunnable(long priority) {
        this(null, priority);
    }

    public String getName() {
        return name == null ? this.toString() : name;
    }

    @Override
    public final void run() {
        if (JobPool.getInstance().getMaxRetryCount() > 0) {
            if (getRetryCount() > JobPool.getInstance().getMaxRetryCount()) {
                failedWork("-1001", "The retry count more than maxRetryCount(" + JobPool.getInstance().getMaxRetryCount() +")");
                return;
            }
            addRetryCount();
        }

        beforeWork();
        Debug.verbose("线程" + Thread.currentThread().getName() + "，开始执行任务" + info());
        long startTime = System.currentTimeMillis();
        work();
        Debug.verbose("线程" + Thread.currentThread().getName() + "，结束执行任务" + info());
        Debug.debug("线程" + Thread.currentThread().getName() + "，执行任务" + info() + "，耗时" + (System.currentTimeMillis() - startTime) + "毫秒");
        afterWork();
    }

    protected void beforeWork() {

    }

    protected void failedWork(String code, String msg){

    }

    protected void afterWork() {
        JobPool.getInstance().removeTaskFromPostedCache(key());
    }

    public abstract void work();

    @Override
    public int compareTo(JobRunnable o) {
        long n = this.priority - o.priority;

        if (n > 0) {
            return 1;
        } else if (n < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public String info(){
        return (name == null ? getClass().getSimpleName() : name) + "-" + this.toString();
    }

    public String key(){
        return String.valueOf(hashCode());
    }

    public long getTime() {
        return time;
    }

    public void addRetryCount() {
        if (retryCount < 0) {
            retryCount = 0;
        }
        retryCount ++;
    }

    public int getRetryCount() {
        return retryCount;
    }
}