package thesis;

import com.charrey.Configuration;
import com.charrey.TestCaseProvider;
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
                                boolean continueOnError,
                                boolean calloutEachResult) throws InterruptedException {
        Portfolio portfolio = new Portfolio();
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(512);
                List<Integer> x = new ArrayList<>();
                List<Double> contractionComparedToNot = new ArrayList<>();
                List<Double> portFolioComparedToNot = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random perXRandom = new Random(threadRandom.nextLong());
                    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (contractionComparedToNot.isEmpty() ? "" : ", cases done = " + lastCasesDone + "; last result = " + contractionComparedToNot.get(contractionComparedToNot.size()-1) ));
                    long timeStartForThisX = System.currentTimeMillis();
                    double totalTimeFirst = 0d;
                    double totalTimeSecond = 0d;
                    double totalPortfolio = 0d;
                    int cases = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                        cases++;
                        long testcaseSeed = perXRandom.nextLong();
                        TestCase tc = tcp.get(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed, labels);
                        HomeomorphismResult resultFirst;
                        HomeomorphismResult resultSecond = null;
                        double periodFirst = -1;
                        double periodSecond = -1;
                        try {
                            long startTime = System.nanoTime();
                            resultFirst = SystemTest.testWithoutExpectation(tc, timeout, configuration.getFirst());
                            periodFirst = System.nanoTime() - startTime;
                            if (configuration.getSecond() != null) {
                                startTime = System.nanoTime();
                                resultSecond = SystemTest.testWithoutExpectation(tc, timeout, configuration.getSecond());
                                periodSecond = System.nanoTime() - startTime;
                            }
                        } catch (Exception | Error e) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            e.printStackTrace();
                            if (continueOnError) {
                                continue;
                            } else {
                                throw e;
                            }
                        }
                        if (resultSecond instanceof FailResult || resultFirst instanceof FailResult) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            if (!continueOnError) {
                                throw new IllegalStateException("Failed!");
                            }
                        } else if (resultFirst instanceof SuccessResult && (configuration.getSecond() == null || resultSecond instanceof SuccessResult)) {
                            totalTimeFirst += periodFirst;
                            totalTimeSecond += periodSecond;
                            portfolio.register(currentX, cases, (Math.min(periodFirst, periodSecond) / periodSecond) - 1d, false);
                            if (calloutEachResult) {
                                System.out.println("Success");
                            }
                        }
                    }
                    if (totalTimeFirst > 0 && totalTimeSecond > 0) {
                        contractionComparedToNot.add(100* (totalTimeFirst / totalTimeSecond) - 100d);
                        portFolioComparedToNot.add(100*(totalPortfolio / totalTimeSecond) - 100d);
                        x.add(currentX);
                    }
                    lastCasesDone = cases;
                    currentX++;
                }
                System.out.println(configuration.getString(x, contractionComparedToNot));
            });
            threads.put(configuration, theThread);
        }
        threads.values().forEach(Thread::start);
        for (Thread thread : threads.values()) {
            thread.join();
        }
        System.out.println(portfolio);
    }

    static TestCase getRandomSuccessDirectedTestCase(int vs, int es, int vt, int et, long seed, boolean labels) {
        RandomSucceedDirectedTestCaseGenerator2 gen = new RandomSucceedDirectedTestCaseGenerator2(vs, es, vt, et, seed, labels);
        gen.init(1);
        return gen.getNext();
    }


}
