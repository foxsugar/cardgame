package com.code.server.cardgame.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by win7 on 2017/3/9.
 */
public class ThreadPool {
    private static ThreadPool instance;
    public Executor executor = Executors.newCachedThreadPool();
    private ThreadPool(){}

    static{
        instance = new ThreadPool();
    }

    public static ThreadPool getInstance(){
        return instance;
    }
}
