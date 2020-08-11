package com.cilys.utils.job;

public class JobRunnable implements Runnable, Comparable<JobRunnable>{
    private int priority;
    private String taskName;

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public JobRunnable(){

    }

    public JobRunnable(int priority, String taskName) {
        this.priority = priority;
        this.taskName = taskName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public void run() {
        Debug.println("VERBOSE", null, "线程：" + Thread.currentThread().getName() + "执行" + (taskName == null ? "" : taskName) + "任务...", null);
    }

    @Override
    public int compareTo(JobRunnable jobRunnable) {
        //倒序，越大越先执行
//        return this.getPriority() < jobRunnable.getPriority() ? -1 : (this.getPriority() == jobRunnable.getPriority() ? 0 : 1);
        //正序，越小越先执行
        return this.getPriority() < jobRunnable.getPriority() ? 1 : (this.getPriority() == jobRunnable.getPriority() ? 0 : -1);
    }
}