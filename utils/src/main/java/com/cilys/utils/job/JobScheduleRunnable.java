package com.cilys.utils.job;

/**
 * 负责调度
 */
public class JobScheduleRunnable extends JobRunnable {
    private static JobScheduleRunnable instance;

    public JobScheduleRunnable(){
        instance = this;
        setTaskName("调度");
    }

    public JobScheduleRunnable(int priority, String taskName) {
        super(priority, taskName);
        instance = this;
    }

    public static JobScheduleRunnable getInstance() {
        return instance;
    }

    private int step = 1;

    @Override
    public void run() {
        while (!JobThreadPools.getInstance().isStop()) {
            super.run();
            if (JobThreadPools.getInstance().cacheNotFull()) {
                JobQueue queue = JobQueue.getInstance();
                Object q = queue.poll();
                if(q != null) {
                    if (q instanceof JobWorkerRunnable) {
                        JobWorkerRunnable task = (JobWorkerRunnable) q;

                        if (task != null) {
                            JobThreadPools.getInstance().execute(task);
                            step = 1;
                        } else {
                            if (step < 1000) {
                                step *= 2;
                            }
                        }
                    } else if (q instanceof Integer) {
                        Debug.println("VERBOSE", null, "调度器取出的值：" + q, null);
                    }
                }
            }

            synchronized (this) {
                try {
                    this.wait(30 * step);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
