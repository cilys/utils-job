package com.cilys.utils.job;

import com.cilys.utils.job.impl.WorkResultImpl;

public abstract class WorkJobRunnable<T> extends SingleJobRunnable {
    private String key;
    private WorkResultImpl<T> impl;

    public WorkJobRunnable(String name, String key, WorkResultImpl<T> impl) {
        this(name, 10, key, impl);
    }

    public WorkJobRunnable(String name, long priority, String key, WorkResultImpl<T> impl) {
        super(name, priority);
        this.key = key;
        this.impl = impl;
    }

    public WorkJobRunnable(long priority, String key, WorkResultImpl<T> impl) {
        this(null, priority, key, impl);
    }

    public WorkJobRunnable(String key, WorkResultImpl<T> impl) {
        this(null, key, impl);
    }

    public WorkJobRunnable(String key) {
        this(key, null);
    }

    public WorkJobRunnable(){
        this(null);
    }

    @Override
    protected void beforeWork() {
        super.beforeWork();

        onStart();
    }

    @Override
    protected void afterWork() {
        super.afterWork();
    }

    public void onReadly() {
        if (impl != null) {
            impl.onReadly(key);
        }
    }

    public void inProgress(long currentProgress, long totalProgress) {
        if (impl != null) {
            impl.inProgress(key, currentProgress, totalProgress);
        }
    }

    public void onStart(){
        if (impl != null) {
            impl.onStart(key);
        }
    }

    @Override
    protected void failedWork(String code, String msg) {
        super.failedWork(code, msg);
        onFailure(code, msg);
    }

    public void onSuccess(T t) {
        if (impl != null) {
            impl.onSuccess(key, t);
        }
        onFinish();
    }

    public void onFailure(String errorCode, String errorMsg) {
        if (impl != null) {
            impl.onFailure(key, errorCode, errorMsg);
        }
        onFinish();
    }

    public void onFinish(){
        if (impl != null) {
            impl.onFinish(key);
        }
    }
}
