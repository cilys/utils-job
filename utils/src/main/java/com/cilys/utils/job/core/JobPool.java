package com.cilys.utils.job.core;

import com.cilys.utils.job.SingleJobRunnable;
import com.cilys.utils.job.WorkJobRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobPool {
    private static JobPool instance;

    private ThreadPoolExecutor threadPool;

    public final int maxPoolSize;

    private PriorityBlockingQueue<JobRunnable> taskCache;

    private ConcurrentHashMap<String, JobRunnable> postedJobCache;

    private JobPool(int maxPoolSize){
        this.maxPoolSize = maxPoolSize;

        Debug.verbose("初始化JobService，最大线程数量：" + maxPoolSize);

        initThreadPool();
        initTaskCache();
    }
    private synchronized void initThreadPool(){
        Debug.verbose("开始初始化线程池..");
        threadPool = new ThreadPoolExecutor(0, getMaxPoolSize(),
                30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        Debug.verbose("完成初始化线程池..");
        try {
            SecheduRunnable secheduRunnable = new SecheduRunnable();
            threadPool.execute(secheduRunnable);
            addTaskToPostedCache(secheduRunnable);
            Debug.debug("初始化线程池，添加调度任务成功..");
        } catch (Exception e){
            Debug.error("初始化线程池，添加调度任务时出错：", e);
        }

    }
    private synchronized void initTaskCache(){
        Debug.verbose("开始初始化任务池..");
        taskCache = new PriorityBlockingQueue<>(getMaxPoolSize() * 50);

        if (postedJobCache == null) {
            postedJobCache = new ConcurrentHashMap<>();
        }

        Debug.verbose("完成初始化任务池..");
    }
    private void resetCache(){
        if (taskCache != null) {
            taskCache.clear();
        }
        taskCache = null;
        if (postedJobCache != null) {
            postedJobCache.clear();
        }
        postedJobCache = null;
    }
    public static JobPool getInstance() {
        return getInstance(5);
    }
    public static JobPool getInstance(int maxPoolSize) {
        if (instance == null) {
            synchronized (JobPool.class) {
                if (instance == null) {
                    instance = new JobPool(maxPoolSize);
                }
            }
        }
        return instance;
    }

    public int getMaxPoolSize() {
        return maxPoolSize < 1 ? 5 : maxPoolSize;
    }

    public void shutdown(){
        try{
            if (threadPool != null) {
                if (!threadPool.isShutdown()) {
                    threadPool.shutdown();
                }
            }
            threadPool = null;
            Debug.verbose("关闭线程池..");
        } catch (Exception e){
            Debug.error("关闭线程池出错：", e);
        }
        resetCache();
    }

    protected int getThreadPoolActiveCount(){
        return threadPool == null ? 0 : threadPool.getActiveCount();
    }

    public void shutdownNow(){
        try{
            if (threadPool != null) {
                if (!threadPool.isShutdown()) {
                    threadPool.shutdownNow();
                }
            }
            threadPool = null;
            Debug.verbose("立即关闭线程池..");
        } catch (Exception e){
            Debug.error("立即关闭线程池出错：", e);
        }
        resetCache();
    }

    protected JobRunnable getTask(){
        if (taskCache == null) {
            return null;
        }
        try {
            return taskCache.poll();
        } catch (Exception e) {
            Debug.verbose("从队列里取出任务出错：", e);
            return null;
        }
    }

    public synchronized void addTask(JobRunnable runnable) {
        if (runnable == null) {
            Debug.debug("添加任务时，任务为空：" + runnable);
            return;
        }

        if (taskCache == null) {
            Debug.verbose("添加任务时，任务池为空，准备初始化..");
            initTaskCache();
        }
        try {
            taskCache.offer(runnable);

            if (runnable instanceof WorkJobRunnable) {
                ((WorkJobRunnable) runnable).onReadly();
                ((WorkJobRunnable) runnable).inProgress(0, 0);
            }
            Debug.verbose("任务" + runnable.info() + "加入到队列里..");
            addTaskToPostedCache(runnable);
        } catch (Exception e){
            Debug.warn("添加任务到队列里时出错：", e);
        }

        if (threadPool == null || threadPool.isShutdown()) {
            if (threadPool == null) {
                Debug.verbose("添加任务时，线程池为空：" + threadPool);
            } else {
                Debug.verbose("添加任务时，线程池关闭状态：" + threadPool.isShutdown());
            }
            threadPool = null;
            Debug.verbose("添加任务时，已重置threadPool，准备初始化线程池..");
            initThreadPool();
        } else {
            Debug.verbose("添加任务时，检查threadPool.getActiveCount() = " + threadPool.getActiveCount());
            Debug.verbose("添加任务时，检查threadPool.getQueue().size() = " + threadPool.getActiveCount());
            if (threadPool.getActiveCount() < 1 && threadPool.getQueue().size() < 1) {
                try {
                    SecheduRunnable secheduRunnable = new SecheduRunnable();
                    threadPool.execute(secheduRunnable);
                    addTaskToPostedCache(secheduRunnable);
                    Debug.debug("添加任务时，重新添加调度任务成功..");
                } catch (Exception e){
                    Debug.error("添加任务时，添加调度任务时出错：", e);
                }
            } else {
                if (taskExistsInPostedCache(SecheduRunnable.KEY)) {
                    Debug.debug("添加任务时，线程池有非空闲任务，判断调度任务已存在，不重新添加调度任务..");
                } else {
                    try {
                        SecheduRunnable secheduRunnable = new SecheduRunnable();
                        threadPool.execute(secheduRunnable);
                        addTaskToPostedCache(secheduRunnable);
                        Debug.debug("添加任务时，线程池有非空闲任务，判断调度任务不存在，重新添加调度任务成功..");
                    } catch (Exception e){
                        Debug.error("添加任务时，线程池有非空闲任务，判断调度任务不存在，重新添加调度任务出错：", e);
                    }
                }
            }
        }
    }
    private void addTaskToPostedCache(JobRunnable runnable){
        if (runnable == null) {
            return;
        }
        if (postedJobCache == null) {
            postedJobCache = new ConcurrentHashMap<>();
        }
        if (runnable.key() == null) {
            return;
        }
        if (runnable instanceof SingleJobRunnable) {
            SingleJobRunnable r = (SingleJobRunnable)postedJobCache.get(runnable.key());
            if (r == null) {
                postedJobCache.put(runnable.key(), runnable);
            } else {
                if (r.isOutTime()) {
                    postedJobCache.put(runnable.key(), runnable);
                }
            }
        } else {
            postedJobCache.put(runnable.key(), runnable);
        }
    }
    private boolean taskExistsInPostedCache(String key){
        return taskInPostedCache(key) != null;
    }

    public synchronized JobRunnable taskInPostedCache(String key) {
        if (key == null) {
            return null;
        }
        if (postedJobCache == null) {
            return null;
        }
        return postedJobCache.get(key);
    }

    protected void removeTaskFromPostedCache(String key) {
        if (key == null) {
            return;
        }
        if (postedJobCache == null) {
            return;
        }
        postedJobCache.remove(key);
    }

    protected void execute(JobRunnable runnable){
        if (runnable == null) {
            Debug.verbose("线程池执行任务时，任务为空：" + runnable);
            return;
        }

        if (threadPool == null || threadPool.isShutdown()) {
            Debug.verbose("线程池执行任务时，线程池为null或线程池已关闭，即将重新初始化线程池..");
            threadPool = null;
            initThreadPool();
        }

        try {
            Debug.verbose("线程池开始执行任务：" + runnable.info());
            threadPool.execute(runnable);
            Debug.verbose("线程池已执行任务：" + runnable.info());
        } catch (Exception e) {
            if (e instanceof RejectedExecutionException) {
                if (runnable instanceof JobRunnable) {
                    Debug.verbose("线程池执行任务时被拒绝，任务重新加入到队列里..");
                    addTask((JobRunnable) runnable);
                } else {
                    Debug.verbose("线程池执行任务时被拒绝，非工作任务，直接丢弃..");
                }
            } else {
                Debug.warn("线程池执行任务时出错：" + runnable.info(), e);
            }
        }
    }

    private int maxRetryCount = 0;

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setWaitTime(long waitTime) {
        SecheduRunnable.setWaitTime(waitTime);
    }

    public void setLogLevel(int logLevel){
        Debug.setLogLevel(logLevel);
    }
}