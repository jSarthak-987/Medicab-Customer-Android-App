package com.brightsky.medicab;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorThreadService {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private static ExecutorThreadService executorThreadService = null;


    private ExecutorThreadService() {}

    public static ExecutorThreadService getInstance() {
        if(executorThreadService == null) {
            executorThreadService = new ExecutorThreadService();
        }

        return executorThreadService;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Handler getHandler() {
        return mainThreadHandler;
    }
}
