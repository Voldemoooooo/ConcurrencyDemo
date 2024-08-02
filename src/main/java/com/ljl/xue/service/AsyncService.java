package com.ljl.xue.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * @author ljl
 */
@Service
public class AsyncService {

    @Async
    public CompletableFuture<String> work1Async() throws InterruptedException {
        Thread.sleep(1500);
        System.out.println("事件1完成");
        return CompletableFuture.completedFuture("事件1完成");
    }

    @Async
    public CompletableFuture<String> work2Async() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("事件2完成");
        return CompletableFuture.completedFuture("事件2完成");
    }

    @Async
    public CompletableFuture<String> work3Async() throws InterruptedException {
        Thread.sleep(1100);
        System.out.println("事件3完成");
        return CompletableFuture.completedFuture("事件3完成");
    }


    public String work1() throws InterruptedException {
        Thread.sleep(1500);
        System.out.println("事件1完成");
        return "事件1完成";
    }

    public String work2() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("事件2完成");
        return "事件2完成";
    }

    public String work3() throws InterruptedException {
        Thread.sleep(1100);
        System.out.println("事件3完成");
        return "事件3完成";
    }
}

