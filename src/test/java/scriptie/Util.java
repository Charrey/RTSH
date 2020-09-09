package scriptie;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator2;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import system.SystemTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class Util {

    static void comparitiveTest(List<Configuration> configurations,
                                double sourceDegree,
                                double sizeFactor,
                                double targetdegree,
                                boolean labels,
                                TestCaseProvider tcp,
                                long timeout,
                                boolean continueOnError, boolean calloutEachResult) throws InterruptedException {
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(512);
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random perXRandom = new Random(threadRandom.nextLong());
                    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + ", cases done = " + lastCasesDone);
                    long timeStartForThisX = System.currentTimeMillis();
                    double totalTimeWith = 0d;
                    double totalTimeWithout = 0d;
                    int cases = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout) {
                        cases++;
                        long testcaseSeed = perXRandom.nextLong();
                        TestCase tc = tcp.get(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed, labels);
                        assert tc.getSourceGraph().vertexSet().size() == currentX;
                        if (cases < 14) {
                            continue;
                        }
                        HomeomorphismResult resultWith;
                        HomeomorphismResult resultWithout = null;
                        double periodWith;
                        double periodWithout = 0;
                        try {
                            assert tc.getSourceGraph().vertexSet().size() == currentX;
                            long startTime = System.nanoTime();
                            resultWith = SystemTest.testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                            periodWith = System.nanoTime() - startTime;
                            assert tc.getSourceGraph().vertexSet().size() == currentX;
                            if (configuration.getSettingsWithoutContraction() != null) {
                                assert tc.getSourceGraph().vertexSet().size() == currentX;
                                startTime = System.nanoTime();
                                resultWithout = SystemTest.testWithoutExpectation(tc, timeout, configuration.getSettingsWithoutContraction());
                                periodWithout = System.nanoTime() - startTime;
                                assert tc.getSourceGraph().vertexSet().size() == currentX;
                            }
                        } catch (Exception | Error e) {
                            assert tc.getSourceGraph().vertexSet().size() == currentX;
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            e.printStackTrace();
                            if (continueOnError) {
                                continue;
                            } else {
                                throw e;
                            }
                        }
                        if (resultWithout instanceof FailResult || resultWith instanceof FailResult) {
                            assert tc.getSourceGraph().vertexSet().size() == currentX;
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            if (!continueOnError) {
                                throw new IllegalStateException("Failed!");
                            }
                        } else if (resultWith instanceof SuccessResult && (configuration.getSettingsWithoutContraction() == null || resultWithout instanceof SuccessResult)) {
                            totalTimeWith += periodWith;
                            totalTimeWithout += periodWithout;
                            if (calloutEachResult) {
                                System.out.println("Success");
                            }
                        }
                    }
                    if (totalTimeWith > 0 && totalTimeWithout > 0) {
                        results.add((totalTimeWith / totalTimeWithout) - 1d);
                        x.add(currentX);
                    }
                    lastCasesDone = cases;
                    currentX++;
                    System.out.println(configuration.getString(x, results));
                }
                System.out.println(configuration.getString(x, results));
            });
            threads.put(configuration, theThread);
        }
        threads.values().forEach(Thread::start);
        for (Thread thread : threads.values()) {
            thread.join();
        }
    }

    static TestCase getRandomSuccessDirectedTestCase(int vs, int es, int vt, int et, long seed, boolean labels) {
        RandomSucceedDirectedTestCaseGenerator2 gen = new RandomSucceedDirectedTestCaseGenerator2(vs, es, vt, et, seed, labels);
        gen.init(1);
        return gen.getNext();
    }


}
