package com.ljl.xue.controller;

import com.ljl.xue.service.AsyncService;
import com.ljl.xue.service.InMemoryCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * @author ljl
 */
@RestController
public class XAllController {

    @Autowired
    private AsyncService asyncService;
    @Autowired
    private InMemoryCounter inMemoryCounter;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
/*
 *
 * Java的同步、异步、多线程
 * */
    //同步执行
    @GetMapping("/s1")
    private String s1() throws InterruptedException {
        System.out.println("s1被调用→→→→→→→→→→→→→→→→→→");
        asyncService.work1();
        asyncService.work2();
        asyncService.work3();
        System.out.println("s1返回");
        return "事件1、2、3全部完成";
    }

    //同步执行 + SseEmitter通信
    @GetMapping(value = "/s2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter s2() {
        System.out.println("s2被调用→→→→→→→→→→→→→→→→→→");
        SseEmitter emitter = new SseEmitter();
        //开一个线程异步执行内容，不影响连接的建立，否则同步执行一次返回结果 失去了阶段返回的意义
        new Thread(() -> {
            try {
                emitter.send(asyncService.work1());
                emitter.send(asyncService.work2());
                emitter.send(asyncService.work3());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
/*        // complete() 示例
        emitter.onCompletion(() -> {
            System.out.println("连接正常完成");
        });

        // onTimeout(Runnable callback) 示例
        emitter.onTimeout(() -> {
            System.out.println("连接超时");
        });

        // onError(ErrorCallback callback) 示例
        emitter.onError((Throwable t) -> {
            System.out.println("发生错误: " + t.getMessage());
        });*/
        }).start();
        return emitter;
    }
    //异步执行
    @GetMapping("/s3")
    private String s3() throws InterruptedException {
        System.out.println("s3被调用→→→→→→→→→→→→→→→→→→");
        asyncService.work1Async();
        asyncService.work2Async();
        asyncService.work3Async();
        System.out.println("s3返回");
        return "请求完成回调";
    }

    //异步执行 + SseEmitter通信
    @GetMapping(value = "/s4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter s4() throws InterruptedException, IOException {
        System.out.println("s4被调用→→→→→→→→→→→→→→→→→→");
        SseEmitter emitter = new SseEmitter();

        // 定义 CompletableFuture 列表
        CompletableFuture<Void> future1 = asyncService.work1Async()
                .thenAccept(result -> {
                    try {
                        emitter.send(result);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });

        CompletableFuture<Void> future2 = asyncService.work2Async()
                .thenAccept(result -> {
                    try {
                        emitter.send(result);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });

        CompletableFuture<Void> future3 = asyncService.work3Async()
                .thenAccept(result -> {
                    try {
                        emitter.send(result);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });

        // 等待所有任务完成
        CompletableFuture.allOf(future1, future2, future3)
                .whenComplete((v, t) -> {
                    if (t != null) {
                        emitter.completeWithError(t);
                    } else {
                        try {
                            emitter.send("总用时");
                            emitter.complete(); // 完成
                            System.out.println("所有任务完成，连接关闭");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        emitter.send("请求完成回调");
        return emitter;
    }


    //多线程 new Thread + SseEmitter通信
    /*
    * 其他并发都可以判断全部执行完后的回调，new Thread不支持，适合无关紧要的小分支操作
    * */
    @GetMapping(value = "/s5", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter s5() throws IOException, InterruptedException {
        System.out.println("s5被调用→→→→→→→→→→→→→→→→→→");
        SseEmitter emitter = new SseEmitter();
        // 初始化计数器为3
        CountDownLatch latch = new CountDownLatch(3);
        new Thread(() -> {
            try {
                Runnable task = () -> {
                    try {
                        emitter.send(asyncService.work1());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    } finally {
                        // 线程完成，计数器减1
                        latch.countDown();
                    }
                };
                new Thread(() -> {
                    try {
                        emitter.send(asyncService.work1());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    } finally {
                        // 线程完成，计数器减1
                        latch.countDown();
                    }
                }).start();
                new Thread(() -> {
                    try {
                        emitter.send(asyncService.work2());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    } finally {
                        // 线程完成，计数器减1
                        latch.countDown();
                    }
                }).start();
                new Thread(() -> {
                    try {
                        emitter.send(asyncService.work3());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    } finally {
                        // 线程完成，计数器减1
                        latch.countDown();
                    }
                }).start();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            try {
                // 等待所有线程完成
                latch.await();
                emitter.send("总用时");
                emitter.complete();
                System.out.println("所有任务完成，连接关闭");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        emitter.send("请求完成回调");
        return emitter;
    }
    // 使用线程池 + SseEmitter通信
    @GetMapping(value = "/s6", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter s6() throws IOException {
        System.out.println("s6被调用→→→→→→→→→→→→→→→→→→");
        SseEmitter emitter = new SseEmitter();

        // 提交任务到线程池
        Future<?> future1 = executorService.submit(() -> {
            try {
                emitter.send(asyncService.work1());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        Future<?> future2 = executorService.submit(() -> {
            try {
                emitter.send(asyncService.work2());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        Future<?> future3 = executorService.submit(() -> {
            try {
                emitter.send(asyncService.work3());
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 等待所有任务完成
        executorService.submit(() -> {
            try {
                future1.get();
                future2.get();
                future3.get();
                emitter.send("总用时");
                emitter.complete(); // 完成
                System.out.println("所有任务完成，连接关闭");
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        emitter.send("请求完成回调");
        return emitter;
    }


/*
*
* 初识Java锁机制
* */

    // 线程不安全
    @GetMapping("/incrementUnsafe")
    public String incrementUnsafe() {
        inMemoryCounter.clear();
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                inMemoryCounter.incrementUnsafe();
                try {
                    Thread.sleep(1); // 模拟操作延迟
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        Thread t3 = new Thread(task);
        Thread t4 = new Thread(task);
        Thread t5 = new Thread(task);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int finalValue = inMemoryCounter.getValue();
        System.out.println("成功插入: " + finalValue+" 条");
        return "成功插入: " + finalValue+" 条";
    }

    // 线程安全 synchronized修饰
    @GetMapping("/incrementSafe")
    public String incrementSafe() {
        inMemoryCounter.clear();
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                inMemoryCounter.incrementSafe();
                try {
                    Thread.sleep(1); // 模拟操作延迟
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        Thread t3 = new Thread(task);
        Thread t4 = new Thread(task);
        Thread t5 = new Thread(task);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int finalValue = inMemoryCounter.getValue();
        System.out.println("成功插入: " + finalValue+" 条");
        return "成功插入: " + finalValue+" 条";
    }

    // 线程安全 synchronized修饰  不适用线程 用作速度对比
    @GetMapping("/incrementSafe2")
    public String incrementSafe2() throws InterruptedException {
        inMemoryCounter.clear();
            for (int i = 0; i < 5000; i++) {
                // 模拟操作延迟
                Thread.sleep(1);
                inMemoryCounter.incrementSafe();
            }
        int finalValue = inMemoryCounter.getValue();
        System.out.println("成功插入: " + finalValue+" 条");
        return "成功插入: " + finalValue+" 条";
    }

}
