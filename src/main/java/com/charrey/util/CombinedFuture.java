package com.charrey.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class CombinedFuture implements Future<CombinedResult> {

    private final Future<Double> future1;
    private final Future<Double> future2;
    private final Predicate<Double> isValid;

    public CombinedFuture(Future<Double> future1, Future<Double> future2, Predicate<Double> isValid) {
        this.future1 = future1;
        this.future2 = future2;
        this.isValid = isValid;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        Future<Boolean> stop1 = threadPool.submit(() -> future1.cancel(mayInterruptIfRunning));
        Future<Boolean> stop2 = threadPool.submit(() -> future2.cancel(mayInterruptIfRunning));
        try {
            return stop1.get() && stop2.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        return future1.isCancelled() && future2.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future1.isDone() || future2.isDone();
    }

    public static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Override
    public CombinedResult get() {
        Object lock = new Object();
        AtomicBoolean doneCount = new AtomicBoolean(false);
        final Double[] results = new Double[2];
        synchronized (lock) {
            for (int i = 0; i < 2; i++) {
                int finalI = i;
                threadPool.submit(() -> {
                    try {
                        results[finalI] = List.of(future1, future2).get(finalI).get();
                        if (!doneCount.compareAndSet(false, true)) {
                            synchronized (lock) {
                                lock.notify();
                            }
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
            }
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threadPool.submit(() -> future1.cancel(true));
        threadPool.submit(() -> future2.cancel(true));
        if (isValid.test(results[0]) && isValid.test(results[1])) {
            return new CombinedResult(results[0] < results[1], results[1] < results[0],Math.min(results[0], results[1]));
        } else {
            if (isValid.test(results[0])) {
                return new CombinedResult(true, false, results[0]);
            }
            return new CombinedResult(false, results[1] != null && isValid.test(results[1]), results[1]);
        }
    }

    @Override
    public CombinedResult get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }


}
