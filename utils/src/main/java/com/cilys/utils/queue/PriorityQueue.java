package com.cilys.utils.queue;

import com.cilys.utils.job.Debug;
import com.cilys.utils.job.JobRunnable;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PriorityQueue<T> {
    private final int DEFAULT_CACPCITY = 5000;

    private PriorityBlockingQueue<T> workQueue;
    private PriorityBlockingQueue<T> cacheQueue;

    public PriorityQueue(){
        workQueue = new PriorityBlockingQueue<>(DEFAULT_CACPCITY);
        cacheQueue = new PriorityBlockingQueue<>(DEFAULT_CACPCITY * 9);
    }

    public boolean offer(T t) {
        if (t == null){
            return false;
        }
        if (workQueue == null) {
            workQueue = new PriorityBlockingQueue<>(DEFAULT_CACPCITY);
        }
        if (workQueue.size() >= DEFAULT_CACPCITY) {
            if (cacheQueue == null) {
                cacheQueue = new PriorityBlockingQueue<>(DEFAULT_CACPCITY * 9);
            }
            if (cacheQueue.size() > DEFAULT_CACPCITY * 9){
                return false;
            } else {
                if (t instanceof JobRunnable) {
                    Debug.println("VERBOSE", null, ((JobRunnable) t).getTaskName() + "加入到缓存队列里...", null);
                }
                return cacheQueue.offer(t, 3, TimeUnit.SECONDS);
            }
        } else {
            if (t instanceof JobRunnable) {
                Debug.println("VERBOSE", null, ((JobRunnable) t).getTaskName() + "加入到工作队列里...", null);
            }
            return workQueue.offer(t, 3, TimeUnit.SECONDS);
        }
    }

    public T poll(){
        if (workQueue == null) {
            workQueue = new PriorityBlockingQueue<>(DEFAULT_CACPCITY);
        }
        if (cacheQueue == null) {
            cacheQueue = new PriorityBlockingQueue<>(9 * DEFAULT_CACPCITY);
        }
        T t1 = poll(workQueue);
        if (t1 != null && t1 instanceof JobRunnable) {
            Debug.println("VERBOSE", null, ((JobRunnable) t1).getTaskName() + "从工作队列里取出来了...", null);
        }

        if (t1 == null) {
            t1 = poll(cacheQueue);
            if (t1 != null && t1 instanceof JobRunnable) {
                Debug.println("VERBOSE", null, ((JobRunnable) t1).getTaskName() + "从缓存队列里取出来了...", null);
            }
            return t1;
        } else {
            if (workQueue.size() < DEFAULT_CACPCITY) {
                T t2 = poll(cacheQueue);
                if (t2 != null && t2 instanceof JobRunnable) {
                    Debug.println("VERBOSE", null, ((JobRunnable) t2).getTaskName() + "从缓存队列里取出来了...", null);
                }
                if (t2 != null) {
                    offer(t2);
                }
            }
            return t1;
        }
    }

    private T poll(PriorityBlockingQueue<T> queue) {
        if (queue == null) {
            return null;
        }
        if (queue.size() < 1){
            return null;
        }
        try {
            return queue.poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clear(){
        if (workQueue != null) {
            workQueue.clear();
        }
        if (cacheQueue != null) {
            cacheQueue.clear();
        }

        workQueue = null;
        cacheQueue = null;
    }

    public int size(){
        int size = 0;
        if (workQueue != null){
            size = workQueue.size();
        }
        if (cacheQueue != null) {
            size += cacheQueue.size();
        }
        return size;
    }
}
