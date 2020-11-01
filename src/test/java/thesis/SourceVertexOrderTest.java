package thesis;

import com.charrey.Configuration;
import com.charrey.IsoFinder;
import com.charrey.TestCaseProvider;
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

public class SourceVertexOrderTest {


    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(() -> degreeVsRandom(1.5));
        Thread thread3 = new Thread(() -> degreeVsRandom(3));
        Thread thread5 = new Thread(() -> degreeVsRandom(5));
        thread1.start();
        thread3.start();
        thread5.start();
        thread1.join();
        thread3.join();
        thread5.join();
    }

    public static void degreeVsRandom(double factor) {
        try(FileWriter fw = new FileWriter("greatestconstrainedfirstvsrandom" + factor + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            List<Configuration> configurations = new LinkedList<>();
            configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                    new SettingsBuilder().withKPathRouting().withGreatestConstrainedFirstSourceVertexOrder().get(),
                    new SettingsBuilder().withKPathRouting().withRandomSourceVertexOrder().get()));
            configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                    new SettingsBuilder().withInplaceDFSRouting().withGreatestConstrainedFirstSourceVertexOrder().get(),
                    new SettingsBuilder().withInplaceDFSRouting().withRandomSourceVertexOrder().get()));
            configurations.add(new Configuration("+",        "green" , "CP"       ,
                    new SettingsBuilder().withControlPointRouting().withGreatestConstrainedFirstSourceVertexOrder().get(),
                    new SettingsBuilder().withControlPointRouting().withRandomSourceVertexOrder().get()));
            configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                    new SettingsBuilder().withInplaceOldGreedyDFSRouting().withGreatestConstrainedFirstSourceVertexOrder().get(),
                    new SettingsBuilder().withInplaceOldGreedyDFSRouting().withRandomSourceVertexOrder().get()));
            configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                    new SettingsBuilder().withInplaceNewGreedyDFSRouting().withGreatestConstrainedFirstSourceVertexOrder().get(),
                    new SettingsBuilder().withInplaceNewGreedyDFSRouting().withRandomSourceVertexOrder().get()));
            configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                    new SettingsBuilder().withCachedGreedyDFSRouting().withGreatestConstrainedFirstSourceVertexOrder().get(),
                    new SettingsBuilder().withCachedGreedyDFSRouting().withRandomSourceVertexOrder().get()));
            comparitiveTest(configurations, 2.429, factor, 3.425, false,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, factor, (int)seed).init(1).getNext()
                    , true, false, Util.setOf(System.out, out),  "greatestconstrainedfirstvsrandom" + factor + ".txt");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static long timeout = 30*60*1000;

    static void comparitiveTest(List<Configuration> configurations,
                                double sourceDegree,
                                double sizeFactor,
                                double targetdegree,
                                boolean labels,
                                TestCaseProvider tcp,
                                boolean continueOnError,
                                boolean calloutEachResult,
                                Set<Appendable> outputs,
                                String additionalInfo) throws InterruptedException {
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Object fileLock = new Object();
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(512);
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random perXRandom = new Random(threadRandom.nextLong());

                    String toPrint = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (results.isEmpty() ? "" : ", cases done = " + lastCasesDone+", lastRatio = " + results.get(results.size() - 1));
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
                    double totalTimeWith = 0d;
                    double totalTimeWithout = 0d;
                    double totalPortfolio = 0d;
                    int cases = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                        cases++;
                        long testcaseSeed = perXRandom.nextLong();
                        TestCase tc = tcp.get(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed, labels);
                        HomeomorphismResult resultWith;
                        HomeomorphismResult resultWithout = null;
                        double periodWith = -1;
                        double periodWithout = -1;
                        try {
                            long startTime = System.nanoTime();
                            resultWith = testWithoutExpectation(tc, timeout, configuration.getFirst());
                            periodWith = System.nanoTime() - startTime;
                            if (configuration.getSecond() != null) {
                                startTime = System.nanoTime();
                                resultWithout = testWithoutExpectation(tc, timeout, configuration.getSecond());
                                periodWithout = System.nanoTime() - startTime;
                            }
                        } catch (Exception | Error e) {
                            String error = (additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            synchronized (fileLock) {
                                outputs.forEach(y -> {
                                    try {
                                        y.append(error + "\n");
                                        ((Flushable) y).flush();
                                    } catch (IOException e2) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                            e.printStackTrace();
                            if (continueOnError) {
                                continue;
                            } else {
                                throw e;
                            }
                        }
                        if (resultWithout instanceof FailResult || resultWith instanceof FailResult) {
                            System.out.println(additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            if (!continueOnError) {
                                throw new IllegalStateException("Failed!");
                            }
                        } else if (resultWith instanceof SuccessResult && (configuration.getSecond() == null || resultWithout instanceof SuccessResult)) {
                            totalTimeWith += periodWith;
                            totalTimeWithout += periodWithout;
                            if (calloutEachResult) {
                                System.out.println("Success");
                            }
                        }
                    }
                    if (totalTimeWith > 0 && totalTimeWithout > 0) {
                        results.add(totalTimeWith / totalTimeWithout);
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
            });
            threads.put(configuration, theThread);
        }
        threads.values().forEach(Thread::start);
        for (Thread thread : threads.values()) {
            thread.join();
        }
    }

    @NotNull
    public static HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder(settings).getHomeomorphism(testCase, timeout, "SYSTEMTEST", false);
    }


}
