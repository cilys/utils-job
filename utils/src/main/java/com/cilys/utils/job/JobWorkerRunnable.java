package com.cilys.utils.job;

/**
 * 负责执行具体任务
 */
public class JobWorkerRunnable extends JobRunnable {

    public JobWorkerRunnable(){
        super();
        setTaskName("工作");
    }

    public JobWorkerRunnable(int priority, String taskName){
        super(priority, taskName);
    }

    @Override
    public void run() {
        super.run();

        if (JobScheduleRunnable.getInstance() != null) {
            synchronized (JobScheduleRunnable.getInstance()) {
                JobScheduleRunnable.getInstance().notify();
            }
        }
    }
}
