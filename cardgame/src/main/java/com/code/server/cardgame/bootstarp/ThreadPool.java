package com.code.server.cardgame.bootstarp;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by win7 on 2016/12/27.
 */
public class ThreadPool {
    public static final int MAX_THREAD_NUM = Runtime.getRuntime().availableProcessors() + 1;

    private Map<Integer, Queue> queues = new HashMap<>();




    public static void main(String[] args) {
        int cpuNum = Runtime.getRuntime().availableProcessors();
        System.out.println(cpuNum);
    }
}
