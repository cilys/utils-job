package com.cilys.utils.job.core;

/**
 * 负责调度
 */
class SecheduRunnable extends JobRunnable {
    private static Thread currentThread;
    private int sleepCount = 0;

    private static long waitTime = 40;

    public static void setWaitTime(long waitTime) {
        SecheduRunnable.waitTime = waitTime;
    }

    public static long getWaitTime() {
        return waitTime < 1 ? 40 : waitTime;
    }

    public SecheduRunnable(){
        super(0);
    }

    @Override
    public void work() {
        if (currentThread != null) {
            Debug.verbose(scheduThreadName() + "已存在，不继续执行..");
            return;
        }

        currentThread = Thread.currentThread();
        Debug.verbose(scheduThreadName());

        while (running) {
            boolean noTask = true;

            if (JobPool.getInstance().getThreadPoolActiveCount() < JobPool.getInstance().getMaxPoolSize()) {
                for (int i = 0; i < JobPool.getInstance().getMaxPoolSize(); i++) {
                    JobRunnable r = JobPool.getInstance().getTask();
                    Debug.debug(scheduThreadName() + "调度任务取出工作任务：" + (r == null ? "为空.." : r.info()));
                    if (r == null) {
                        break;
                    }
                    Debug.debug(scheduThreadName() + "调度任务取出工作任务：" + r.info() + "，并提交到线程池执行..");
                    JobPool.getInstance().execute(r);
                    noTask = false;
                }
                Debug.verbose("没有任务需要执行：" + noTask);
            } else {
                noTask = false;
                Debug.debug("线程池活动线程数，大于或等于最大线程数量，当前线程池无空闲线程，调度任务不取出工作任务..");
                sleepCount = 1;
            }


            if (sleepCount > 60) {
                Debug.debug("调度任务已休眠60+次，停止该任务..");
                stop();
            } else {
                synchronized (currentThread) {
                    try {
                        if (noTask) {
                            sleepCount ++;
                            Debug.debug(scheduThreadName() + "没有任务需要执行，开始休眠，休眠次数为" + sleepCount);

                            if (sleepCount <= 20) {
                                //                                currentThread.wait(100 * sleepCount + 100);
                                Debug.verbose(scheduThreadName() + "休眠次数：" + sleepCount + "，开始休眠：" + getWaitTime());
                                currentThread.wait(getWaitTime());
                            } else if (sleepCount > 20 && sleepCount <= 40) {
                                Debug.verbose(scheduThreadName() + "休眠次数：" + sleepCount + "，开始休眠：" + getWaitTime() * 5);
                                currentThread.wait(getWaitTime() * 5);
                            } else {
                                Debug.verbose(scheduThreadName() + "休眠次数：" + sleepCount + "，开始休眠：" + (getWaitTime() * 10 + 3000));
                                currentThread.wait(getWaitTime() * 10 + 30);
                            }
                        } else {
                            sleepCount = 0;
                            Debug.debug(scheduThreadName() + "有任务需要执行，休眠 " + getWaitTime() + " 后重置休眠次数为0..");
                            currentThread.wait(getWaitTime());
                        }
                    } catch (InterruptedException e) {
                        Debug.verbose("调度任务被中断：", e);
                        stop();
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        currentThread = null;
    }

    private String scheduThreadName(){
        return "调度任务执行线程[" + currentThread.getName() + "]   ";
    }

    @Override
    public String key() {
        return KEY;
    }

    public final static String KEY = "SECHEDU_JOB_RUNNABLE";
}
