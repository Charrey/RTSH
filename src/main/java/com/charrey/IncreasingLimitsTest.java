package com.charrey;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class IncreasingLimitsTest {


    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(() -> testIncreasingLimits(1.5));
        Thread thread2 = new Thread(() -> testIncreasingLimits(3));
        Thread thread3 = new Thread(() -> testIncreasingLimits(5));
        thread1.start();
        thread2.start();
        thread3.start();
        thread1.join();
        thread2.join();
        thread3.join();
    }

    public static void testIncreasingLimits(double factor) {
        try(FileWriter fw = new FileWriter("increasingLimits" + factor + ".txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            List<Configuration> configurations = new LinkedList<>();
            configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                    new SettingsBuilder().withKPathRouting().get()));
            configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                    new SettingsBuilder().withInplaceDFSRouting().get()));
            configurations.add(new Configuration("+",        "green" , "CP"       ,
                    new SettingsBuilder().withControlPointRouting().get()));
            configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                    new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
            configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                    new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
            configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                    new SettingsBuilder().withCachedGreedyDFSRouting().get()));
            increasingLimitsTest(configurations, factor,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, factor, (int)seed).init(1).getNext()
                    , Util.setOf(System.out, out),  "increasingLimits" + factor + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static long timeout = 10*60*1000;

    static void increasingLimitsTest(List<Configuration> configurations,
                                     double sizeFactor,
                                     TestCaseProvider tcp,
                                     Set<Appendable> outputs,
                                     String additionalInfo) {
        for (Configuration configuration : configurations) {
            Random threadRandom = new Random(512);
            List<Integer> x = new ArrayList<>();
            List<Double> increasingLimitsComparedToNormal = new ArrayList<>();
            int currentX = 4;
            int lastCasesDone = 10;
            while (lastCasesDone > 1) {
                Random perXRandom = new Random(threadRandom.nextLong());
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " factor="+sizeFactor+", " + configuration + ", x = " + currentX + (increasingLimitsComparedToNormal.isEmpty() ? "" : ", cases done = " + lastCasesDone + ", last ratio = " + increasingLimitsComparedToNormal.get(increasingLimitsComparedToNormal.size()-1)));
                long timeStartForThisX = System.currentTimeMillis();
                double totalTimeNormal = 0d;
                double totalTimeIncreasingLimits = 0d;
                int cases = 0;
                while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 1000) {
                    cases++;
                    long testcaseSeed = perXRandom.nextLong();
                    TestCase tc = tcp.get(currentX, 0, (int)Math.round(currentX * sizeFactor), 0, testcaseSeed, true);
                    HomeomorphismResult resultNormal;
                    HomeomorphismResult resultIncreasingLimits = null;
                    double periodNormal = -1;
                    double periodIncreasingLimits = -1;
                    try {
                        long startTime = System.nanoTime();
                        resultNormal = new IsoFinder().getHomeomorphism(tc, configuration.getSettingsWithContraction(), timeout, "Normal", false);
                        periodNormal = System.nanoTime() - startTime;
                        startTime = System.nanoTime();
                        resultIncreasingLimits = new IncreasingLimits(6).getHomeomorphism(tc, configuration.getSettingsWithContraction(), timeout,  "IncreasingLimits", false);
                        periodIncreasingLimits = System.nanoTime() - startTime;
                    } catch (Exception | Error e) {
                        String error = (additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                        outputs.forEach(y -> {
                            try {
                                y.append(error + "\n");
                                ((Flushable) y).flush();
                            } catch (IOException e2) {
                                e.printStackTrace();
                            }
                        });
                        e.printStackTrace();
                        continue;
                    }
                    if (resultIncreasingLimits instanceof FailResult || resultNormal instanceof FailResult) {
                        System.out.println(additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                    } else if (resultNormal instanceof SuccessResult && (configuration.getSettingsWithoutContraction() == null || resultIncreasingLimits instanceof SuccessResult)) {
                        totalTimeNormal += periodNormal;
                        totalTimeIncreasingLimits += periodIncreasingLimits;
                    }
                }
                if (totalTimeNormal > 0 && totalTimeIncreasingLimits > 0) {
                    increasingLimitsComparedToNormal.add(totalTimeIncreasingLimits / totalTimeNormal);
                    x.add(currentX);
                }
                lastCasesDone = cases;
                currentX++;
            }
            outputs.forEach(y -> {
                try {
                    y.append(configuration.getString(x, increasingLimitsComparedToNormal) + "\n");
                    ((Flushable) y).flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
