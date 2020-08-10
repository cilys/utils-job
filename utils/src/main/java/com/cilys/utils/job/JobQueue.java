package com.cilys.utils.job;

import com.cilys.utils.queue.PriorityQueue;


public class JobQueue<T> {

    private static JobQueue instance;

    public static JobQueue getInstance() {
        if (instance == null) {
            synchronized (JobQueue.class) {
                if (instance == null) {
                    instance = new JobQueue();
                }
            }
        }
        return instance;
    }

    private PriorityQueue<T> queue;
    private JobQueue(){
        queue = new PriorityQueue<>();
    }

    public boolean offer(T t) {
        return queue.offer(t);
    }

    public T poll(){
        return queue.poll();
    }

    public void clear(){
        queue.clear();
    }
}