package thesis;

import com.charrey.Configuration;
import com.charrey.IsoFinder;
import com.charrey.TestCaseProvider;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.CombinedFuture;
import com.charrey.util.CombinedResult;
import com.charrey.util.Util;
import org.junit.jupiter.api.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HighPerformanceTest {


    @Test
    @Order(1)
    public void testSmallest() throws ExecutionException, InterruptedException {
        testHighPerformanceCompute(1.5);
    }

    @Test
    @Order(2)
    public void testSmaller() throws ExecutionException, InterruptedException {
        testHighPerformanceCompute(3d);
    }

    @Test
    @Order(3)
    public void testLarger() throws ExecutionException, InterruptedException {
        testHighPerformanceCompute(5d);
    }

    @Test
    @Order(4)
    public void testLargest() throws ExecutionException, InterruptedException {
        testHighPerformanceCompute(97d);
    }


    public static void testHighPerformanceCompute(double factor) throws ExecutionException, InterruptedException {
        testHighPerformanceWithSettings(factor, false, 4);
    }

    public static void testHighPerformanceMemory(double factor) throws ExecutionException, InterruptedException {
        testHighPerformanceWithSettings(factor, true, 4);
    }

    public static void testHighPerformanceWithSettings(double factor, boolean memory, int minX) throws ExecutionException, InterruptedException {
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
                            .allowingLongerPaths()
                            .get(),
                    new SettingsBuilder()
                            .withInplaceDFSRouting()
                            .withGreatestConstrainedFirstSourceVertexOrder()
                            .withCachedPruning()
                            .withAllDifferentPruning()
                            .withNeighbourReachabilityFiltering()
                            .withoutContraction()
                            .allowingLongerPaths()
                            .get());
            portFolioTest(configuration,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, factor, (int)seed).init(1).getNext(),
                    Util.setOf(System.out, out),
                    memory,
                    minX);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final long timeout = 10*60*1000;

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    static void portFolioTest(Configuration configuration,
                              TestCaseProvider tcp,
                              Set<Appendable> outputs, boolean memory, int minX) throws ExecutionException, InterruptedException {

        Object fileLock = new Object();

        Random threadRandom = new Random(512);
        List<Integer> x = new ArrayList<>();
        List<Double> results = new ArrayList<>();

        int currentX = minX;
        int lastCasesDone = 10;
        int lastTotalSuccess = 10;
        double lastRatio = 0d;
        while (lastTotalSuccess > 1) {
            Random perXRandom = new Random(threadRandom.nextLong());

            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            System.out.println("x =              " + currentX);
            if (!results.isEmpty()) {
                System.out.println("Cases done =     " + lastTotalSuccess);
                System.out.println("Last time =      " + results.get(results.size() - 1));
                System.out.println("Pool size =      " + ((ThreadPoolExecutor)threadPool).getPoolSize() + "/" + CombinedFuture.threadPool.getPoolSize());
                System.out.println("Last ratio =     " + lastRatio);
            }

            System.out.println();

            long timeStartForThisX = System.currentTimeMillis();
            int cases = 0;
            int totalSuccess = 0;
            double totalSpace = 0;

            double total1win = 0d;
            double total2win = 0d;
            long threadStart = System.currentTimeMillis();
            while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < (memory ? 100 : 1000)) {
                cases++;
                System.gc();
                long testcaseSeed = perXRandom.nextLong();
                TestCase tc = tcp.get(currentX, 0, 0, 0, testcaseSeed, false);

                Future<Double> future1 = threadPool.submit(() -> {
                    try {
                        long start = System.currentTimeMillis();
                        HomeomorphismResult result = new IsoFinder(configuration.getFirst()).getHomeomorphism(tc, System.currentTimeMillis() - timeStartForThisX, "THREAD 1", false);
                        long end = System.currentTimeMillis();
                        if (result instanceof SuccessResult) {
                            return memory ? result.memory : (end - start) / 1000d;
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
                        HomeomorphismResult result = new IsoFinder(configuration.getSecond()).getHomeomorphism(tc, System.currentTimeMillis() - timeStartForThisX, "THREAD 1", false);
                        long end = System.currentTimeMillis();
                        if (result instanceof SuccessResult) {
                            return memory ? result.memory : (end - start) / 1000d;
                        } else {
                            return Double.NaN;
                        }
                    } catch (Throwable e) {
                        return Double.NaN;
                    }
                });

                Future<CombinedResult> combinedFuture = new CombinedFuture(future1, future2, y -> y != null && !Double.isNaN(y));
                CombinedResult res = combinedFuture.get();
                if (res.firstWon) {
                    total1win++;
                } else if (res.secondWon) {
                    total2win++;
                }
                if ((memory && res.value > 1d) || (!memory && res.value < Double.MAX_VALUE)) {
                    totalSuccess++;
                    if (memory) {
                        totalSpace += res.value;
                    }
                }
              }
            if (!memory) {
                results.add((cases >= 1000 ? (System.currentTimeMillis() - threadStart) / 1000d : 10 * 60d) / (double) totalSuccess);
            } else {
                results.add(totalSpace / (totalSuccess * 1_000_000));
            }
            x.add(currentX);
            lastTotalSuccess = totalSuccess;
            lastRatio = total2win / (total1win + total2win);
            currentX++;
        }

        synchronized (fileLock) {
            outputs.forEach(y -> {
                try {
                    y.append(configuration.getString(x, results)).append("\n");
                    ((Flushable) y).flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }



}
