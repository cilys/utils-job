package com.cilys.utils.job;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobThreadPools {
    private ThreadPoolExecutor pools;

    private static JobThreadPools instance;

    public static JobThreadPools getInstance(){
        return getInstance(-1);
    }

    public static JobThreadPools getInstance(int cacheCacpcity){
        if (instance == null) {
            synchronized (JobThreadPools.class) {
                if (instance == null) {
                    instance = new JobThreadPools(cacheCacpcity);
                }
            }
        }
        return instance;
    }

    private JobThreadPools(int cacheCacpcity){
        initPools(cacheCacpcity);
    }

    private int cacheCacpcity;
    private void initPools(int cacheCacpcity){
        int cpu = Runtime.getRuntime().availableProcessors();
        int half = cpu / 2;
//        int core = half < 1 ? 1 : half;
        int core = cpu - 1 < 1 ? 1 : cpu - 1;
        if (cacheCacpcity < 1) {
            cacheCacpcity = (cpu + 1) * 10;
        }

        cacheCacpcity = cacheCacpcity < 1 ? 1 : cacheCacpcity;


        this.cacheCacpcity = cacheCacpcity;

        pools = new ThreadPoolExecutor(core, core, 3, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(cacheCacpcity), Executors.defaultThreadFactory(),
                new JobRejected());
        pools.allowCoreThreadTimeOut(true);

    }

    public boolean cacheNotFull(){
        return pools.getQueue().size() < cacheCacpcity;
    }

    public void start(){
        if (pools == null) {
            initPools(-1);
        }
        if (pools.isShutdown()) {
            return;
        }
        pools.execute(new JobScheduleRunnable());
    }

    public void execute(Runnable task){
        if (task == null) {
            return;
        }
        if (pools == null) {
            initPools(-1);
        }
        if (pools != null) {
            pools.execute(task);
        }
    }

    private boolean stop = false;

    public boolean isStop() {
        return stop;
    }

    public void shutdown(){
        if (pools != null) {
            pools.shutdown();
        }
        stop = true;
    }

    public void shutdownNow(){
        if (pools != null) {
            pools.shutdownNow();
        }
        stop = true;
    }

    public void debugLog(boolean debugLog){
        Debug.setDebug(debugLog);
    }
}