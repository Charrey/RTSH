package com.charrey;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HighPerformanceTest {


    public static void main(String[] args) throws InterruptedException {
        testHighPerformance(1.5);
        testHighPerformance(3);
        testHighPerformance(5);
        testHighPerformance(97);
    }

    public static void testHighPerformance(double factor) {
        try(FileWriter fw = new FileWriter("greatestconstrainedfirstvsrandom" + factor + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            Configuration configuration = new Configuration("*",        "blue"  , "High performance"   ,
                    new SettingsBuilder()
                            .withInplaceDFSRouting()
                            .withGreatestConstrainedFirstSourceVertexOrder()
                            .withCachedPruning()
                            .withAllDifferentPruning()
                            .withNeighbourReachabilityFiltering()
                            .withContraction()
                            .get(),
                    new SettingsBuilder()
                            .withInplaceDFSRouting()
                            .withGreatestConstrainedFirstSourceVertexOrder()
                            .withCachedPruning()
                            .withAllDifferentPruning()
                            .withNeighbourReachabilityFiltering()
                            .withoutContraction()
                            .get());
            portFolioTest(configuration,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, factor, (int)seed).init(1).getNext()
                    , Util.setOf(System.out, out),  "greatestconstrainedfirstvsrandom" + factor + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static long timeout = 30*60*1000;

    static void portFolioTest(Configuration configuration,
                              TestCaseProvider tcp,
                              Set<Appendable> outputs,
                              String additionalInfo) {

        Object fileLock = new Object();

        Random threadRandom = new Random(512);
        List<Integer> x = new ArrayList<>();
        List<Double> results = new ArrayList<>();

        int currentX = 4;
        int lastCasesDone = 10;
        while (lastCasesDone > 1) {
            Random perXRandom = new Random(threadRandom.nextLong());
            String toPrint = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (results.isEmpty() ? "" : ", cases done = " + lastCasesDone+", last time = " + results.get(results.size() - 1));
            synchronized (fileLock) {
                outputs.forEach(y -> {
                    try {
                        y.append(toPrint + "\n");
                        ((Flushable) y).flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            long timeStartForThisX = System.currentTimeMillis();
            double totalPortfolio = 0d;
            int cases = 0;
            while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                cases++;
                long testcaseSeed = perXRandom.nextLong();
                TestCase tc = tcp.get(currentX, 0, 0, 0, testcaseSeed, false);
                HomeomorphismResult result1;
                HomeomorphismResult result2 = null;
                double period1 = -1;
                double period2 = -1;
                try {
                    long startTime = System.nanoTime();
                    result1 = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                    period1 = System.nanoTime() - startTime;
                    startTime = System.nanoTime();
                    result2 = testWithoutExpectation(tc, timeout, configuration.getSettingsWithoutContraction());
                    period2 = System.nanoTime() - startTime;

                } catch (Exception | Error e) {
                    System.out.println(additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                    e.printStackTrace();
                    continue;
                }
                if (result2 instanceof FailResult || result1 instanceof FailResult) {
                    System.out.println(additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                } else if (result1 instanceof SuccessResult && result2 instanceof SuccessResult) {
                    totalPortfolio += Math.min(period1, period2);
                }
            }
            if (totalPortfolio > 0) {
                results.add((totalPortfolio / (double)cases) / 1_000_000_000d);
                x.add(currentX);
            }
            lastCasesDone = cases;
            currentX++;
        }
        synchronized (fileLock) {
            outputs.forEach(y -> {
                try {
                    y.append(configuration.getString(x, results) + "\n");
                    ((Flushable) y).flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @NotNull
    public static HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder().getHomeomorphism(testCase, settings, timeout, "SYSTEMTEST", false);
    }


}
