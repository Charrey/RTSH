package com.charrey;

import com.charrey.graph.generation.TestCase;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class IncreasingLimits implements HomeomorphismSolver {

    private final Settings settings;

    private final ThreadPoolExecutor pool;

    public IncreasingLimits(Settings settings, int threads) {
        this.settings = settings;
        this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
    }

    public HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, long timeout, String name, boolean monitorSpace) {
        final HomeomorphismResult[] confirmedFound = {null};
        final HomeomorphismResult[] timedOut = {null};
        final HomeomorphismResult[] confirmedFailed = {null};
        int lastAdded = 0;
        pool.submit(() -> {
            HomeomorphismResult result = new IsoFinder(settings).getHomeomorphism(testcase, timeout, name, false);
            if (result instanceof SuccessResult) {
                confirmedFound[0] = result;
            } else if (result instanceof TimeoutResult) {
                timedOut[0] = result;
            } else if (result instanceof FailResult) {
                confirmedFailed[0] = result;
            }
        });
        while (confirmedFound[0] == null && confirmedFailed[0] == null && timedOut[0] == null) {
            if (pool.getActiveCount() < pool.getPoolSize()) {
                int finalLastAdded = lastAdded;
                pool.submit(() -> {
                    HomeomorphismResult result = new IsoFinder(new SettingsBuilder(settings).withVertexLimit(finalLastAdded +1).withPathsLimit(finalLastAdded +1).get()).getHomeomorphism(testcase, timeout, name, false);
                    if (result instanceof SuccessResult) {
                        confirmedFound[0] = result;
                    } else if (result instanceof TimeoutResult) {
                        timedOut[0] = result;
                    }
                });
                lastAdded++;
            }
        }
        pool.shutdownNow();
        if (confirmedFound[0] != null) {
            return confirmedFound[0];
        } else if (confirmedFailed[0] != null) {
            return confirmedFailed[0];
        } else {
            return timedOut[0];
        }
    }
}
