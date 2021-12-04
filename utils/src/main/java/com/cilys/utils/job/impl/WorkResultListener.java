package com.cilys.utils.job.impl;

public abstract class WorkResultListener<T> implements WorkResultImpl<T> {

    @Override
    public void onReadly(String key) {

    }

    @Override
    public void onStart(String key) {

    }

    @Override
    public void inProgress(String key, long currentProgress, long totalProgress) {

    }

    @Override
    public void onFinish(String key) {

    }
}
