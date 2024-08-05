package com.ljl.xue.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ljl
 * @date 2024/8/5 上午9:40
 * @description SynchronizedDemo
 */

@Service
public class InMemoryCounter {

    // 使用 ConcurrentHashMap 模拟数据库表
    private final ConcurrentHashMap<Integer, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    public  InMemoryCounter() {
        // 初始化计数器
        counterMap.put(1, new AtomicInteger(0));
    }

    // 线程不安全的计数器增加方法
    public  void incrementUnsafe() {
        AtomicInteger counter = counterMap.get(1);
        int currentValue = counter.get();
        int newValue = currentValue + 1;
        counter.set(newValue);
    }

    // 线程安全的计数器增加方法
    public synchronized void incrementSafe() {
        AtomicInteger counter = counterMap.get(1);
        int currentValue = counter.get();
        int newValue = currentValue + 1;
        counter.set(newValue);
    }

    public int getValue() {
        return counterMap.get(1).get();
    }

    public void clear() {
        counterMap.clear();
        counterMap.put(1, new AtomicInteger(0));
    }
}
