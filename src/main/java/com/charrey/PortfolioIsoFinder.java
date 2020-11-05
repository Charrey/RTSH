package com.charrey;

import com.charrey.graph.generation.TestCase;
import com.charrey.result.*;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;


/**
 * Class that finds node disjoint subgraph homeomorphisms
 */
public class PortfolioIsoFinder implements HomeomorphismSolver {

    private final Settings settingsA;
    private final Settings settingsB;


    public PortfolioIsoFinder(Settings settingsA, Settings settingsB) {
        this.settingsA = settingsA;
        this.settingsB = settingsB;
    }

    final ExecutorService threadpool = Executors.newFixedThreadPool(2);
    final CompletionService<HomeomorphismResult> completionService = new ExecutorCompletionService<>(threadpool);



    @Override
    public HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, long timeout, String name, boolean monitorSpace) {
        completionService.submit(() -> new IsoFinder(settingsA).getHomeomorphism(testcase, timeout, name, monitorSpace));
        completionService.submit(() -> new IsoFinder(settingsB).getHomeomorphism(testcase, timeout, name, monitorSpace));
        try {
            Future<HomeomorphismResult> taken = completionService.take();
            HomeomorphismResult toReturn = taken.get();
            threadpool.shutdownNow();
            return toReturn;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
