package com.cilys.utils.job.impl;

public interface WorkResultImpl<T> {
    void onReadly(String key);
    void onStart(String key);
    void inProgress(String key, long currentProgress, long totalProgress);
    void onSuccess(String key, T result);
    void onFailure(String key, String errorCode, String errorMsg);
    void onFinish(String key);
}
