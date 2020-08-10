package com.cilys.utils.job;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class JobRejected implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor threadPoolExecutor) {
        if (task instanceof JobRunnable) {
            Debug.println("VERBOSE", null, " taskName = " + ((JobRunnable)task).getTaskName() + "任务被拒绝", null);

            if (task instanceof JobWorkerRunnable) {
                JobQueue<JobWorkerRunnable> queue = JobQueue.getInstance();
                queue.offer((JobWorkerRunnable) task);
            }
        }
    }
}