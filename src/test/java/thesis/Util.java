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
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(512);
                List<Integer> x = new ArrayList<>();

                List<Double> contractionComparedToNot = new ArrayList<>();
                List<Double> percentageOfWins = new ArrayList<>();

                double lastAdded = Double.NaN;

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random perXRandom = new Random(threadRandom.nextLong());
                    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (contractionComparedToNot.isEmpty() ? "" : ", cases done = " + lastCasesDone + "; last result = " + lastAdded + "; win ratio = " + percentageOfWins.get(percentageOfWins.size()-1) ));
                    lastAdded = Double.NaN;
                    long timeStartForThisX = System.currentTimeMillis();
                    double totalTimeFirst = 0d;
                    double totalTimeSecond = 0d;
                    int cases = 0;
                    int wins = 0;
                    int losses = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                        cases++;
                        long testcaseSeed = perXRandom.nextLong();
                        TestCase tc = tcp.get(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed, labels);
                        HomeomorphismResult resultFirst;
                        HomeomorphismResult resultSecond = null;
                        double periodFirst;
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
                            if (continueOnError) {
                                System.out.println(e.getMessage());
                                continue;
                            } else {
                                System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                                e.printStackTrace();
                                throw e;
                            }
                        }
                        if (resultSecond instanceof FailResult || resultFirst instanceof FailResult) {
                            if (!continueOnError) {
                                System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                                throw new IllegalStateException("Failed!");
                            }
                        } else if (resultFirst instanceof SuccessResult && (configuration.getSecond() == null || resultSecond instanceof SuccessResult)) {
                            totalTimeFirst += periodFirst;
                            totalTimeSecond += periodSecond;
                            if (periodFirst < periodSecond) {
                                wins++;
                            } else {
                                losses++;
                            }
                            if (calloutEachResult) {
                                System.out.println("Success");
                            }
                        } else {
                            cases--;
                        }
                    }
                    if (totalTimeFirst > 0 && totalTimeSecond > 0) {
                        contractionComparedToNot.add(totalTimeFirst / totalTimeSecond);
                        lastAdded = totalTimeFirst / totalTimeSecond;
                        percentageOfWins.add(wins / (double)(wins + losses));
                        x.add(currentX);
                    } else {
                        System.out.println();
                    }
                    lastCasesDone = cases;
                    currentX++;
                    //System.out.println(configuration.getString(x, contractionComparedToNot));
                }
                System.out.println(configuration.getString(x, contractionComparedToNot));
            });
            threads.put(configuration, theThread);
        }
        threads.values().forEach(Thread::start);
        for (Thread thread : threads.values()) {
            thread.join();
        }
    }


}
