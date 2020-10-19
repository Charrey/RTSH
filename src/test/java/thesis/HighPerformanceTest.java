package thesis;

import com.charrey.Configuration;
import com.charrey.IsoFinder;
import com.charrey.TestCaseProvider;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class HighPerformanceTest {


    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(() -> testHighPerformance(1.5)));
        threads.add(new Thread(() -> testHighPerformance(3d)));
        threads.add(new Thread(() -> testHighPerformance(5d)));
        threads.add(new Thread(() -> testHighPerformance(97d)));

        for (Thread thread : threads) {
            thread.start();
            thread.join();
        }
    }

    public static void testHighPerformance(double factor) {
        try(FileWriter fw = new FileWriter("highperformance" + factor + ".txt", true);
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
                    , Util.setOf(System.out, out),  "highperformance" + factor + ".txt");
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    volatile static boolean done = false;

    private static long timeout = 30*60*1000;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(3);

    static void portFolioTest(Configuration configuration,
                              TestCaseProvider tcp,
                              Set<Appendable> outputs,
                              String additionalInfo) throws InterruptedException, ExecutionException {

        Object fileLock = new Object();

        Random threadRandom = new Random(512);
        List<Integer> x = new ArrayList<>();
        List<Double> results = new ArrayList<>();

        int currentX = 4;
        int lastCasesDone = 10;
        while (lastCasesDone > 1) {
            Random perXRandom = new Random(threadRandom.nextLong());
            System.out.print("\n" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (results.isEmpty() ? "" : ", cases done = " + lastCasesDone+", last time = " + results.get(results.size() - 1)));
            long timeStartForThisX = System.currentTimeMillis();
            double totalPortfolio = 0d;
            int cases = 0;
            int totalSuccess = 0;
            while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                cases++;
                long testcaseSeed = perXRandom.nextLong();
                TestCase tc = tcp.get(currentX, 0, 0, 0, testcaseSeed, false);
                Future<Double> future1 = threadPool.submit(() -> {
                    try {
                        long start = System.currentTimeMillis();
                        HomeomorphismResult result = new IsoFinder(configuration.getSettingsWithContraction()).getHomeomorphism(tc, timeout, "THREAD 1", false);
                        long end = System.currentTimeMillis();
                        if (result instanceof SuccessResult) {
                            return (end - start) / 1000d;
                        } else {
                            return Double.NaN;
                        }
                    } catch (Throwable e) {
                        return Double.NaN;
                    }
                });
                Future<Double> future2 = threadPool.submit(() -> {
                    try {
                        long start = System.currentTimeMillis();
                        HomeomorphismResult result = new IsoFinder(configuration.getSettingsWithoutContraction()).getHomeomorphism(tc, timeout, "THREAD 1", false);
                        long end = System.currentTimeMillis();
                        if (result instanceof SuccessResult) {
                            return (end - start) / 1000d;
                        } else {
                            return Double.NaN;
                        }
                    } catch (Throwable e) {
                        return Double.NaN;
                    }
                });
                while (!future1.isDone() && !future2.isDone()) {
                    Thread.sleep(1);
                }
                future1.cancel(true);
                future2.cancel(true);
                double res = Double.MAX_VALUE;
                if (!future1.isCancelled() && !Double.isNaN(future1.get())) {
                    res = Math.min(res, future1.get());
                }
                if (!future2.isCancelled() && !Double.isNaN(future2.get())) {
                    res = Math.min(res, future2.get());
                }
                if (res < Double.MAX_VALUE) {
                    totalSuccess++;
                    totalPortfolio += res;
                } else {
                    System.out.println("failed");
                }
              }
            results.add(totalPortfolio / (double)totalSuccess);
            x.add(currentX);
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
        return new IsoFinder(settings).getHomeomorphism(testCase, timeout, "SYSTEMTEST", false);
    }


}
