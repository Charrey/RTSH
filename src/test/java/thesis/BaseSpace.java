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
import objectexplorer.MemoryMeasurer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseSpace {


    public static void main(String[] args) {
        baseSpace("testcasespace.txt");
    }


    public static void baseSpace(String fileName) { //cr
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
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }




    private static final long timeout = 10*60*1000;

    static void comparitiveTest(List<Configuration> configurations,
                                TestCaseProvider tcp,
                                Set<Appendable> outputs,
                                String additionalInfo) throws InterruptedException {
        for (Configuration configuration : configurations) {
            Object fileLock = new Object();
            Random threadRandom = new Random(512);
            List<Integer> x = new ArrayList<>();
            List<Double> prunedComparedToNot = new ArrayList<>();

            int currentX = 4;
            int lastCasesDone = 10;
            while (lastCasesDone > 1 && currentX < 20) {
                Random perXRandom = new Random(threadRandom.nextLong());
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (prunedComparedToNot.isEmpty() ? "" : ", cases done = " + lastCasesDone + ", last extra space = " + prunedComparedToNot.get(prunedComparedToNot.size() - 1)));
                long timeStartForThisX = System.currentTimeMillis();
                double totalExtraSpace = 0d;
                double totalTcSpace = 0d;

                int cases = 0;
                while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 100) {
                    cases++;
                    long testcaseSeed = perXRandom.nextLong();
                    TestCase tc = tcp.get(currentX, 0, 0, 0, testcaseSeed, false);
                    long caseMemory = MemoryMeasurer.measureBytes(tc);
                    if (tc.hashCode() == 1) {
                        System.out.print("i");
                    }
                    totalTcSpace += caseMemory;
                    HomeomorphismResult resultWithPrune;
                    try {
                        resultWithPrune = testWithoutExpectation(tc, timeout, configuration.getFirst());
                        if (resultWithPrune instanceof FailResult) {
                            System.out.println(additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                        } else if (resultWithPrune instanceof SuccessResult && (configuration.getSecond() == null)) {
                            totalExtraSpace += resultWithPrune.memory;
                        }
                    } catch (Exception | Error e) {
                        String error = (additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                        synchronized (fileLock) {
                            outputs.forEach(y -> {
                                try {
                                    y.append(error).append("\n");
                                    ((Flushable) y).flush();
                                } catch (IOException e2) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        e.printStackTrace();
                    }
                }
                prunedComparedToNot.add(totalExtraSpace / totalTcSpace);
                x.add(currentX);
                lastCasesDone = cases;
                currentX++;
            }
            synchronized (fileLock) {
                outputs.forEach(y -> {
                    try {
                        y.append(configuration.getString(x, prunedComparedToNot)).append("\n");
                        ((Flushable) y).flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        }
    }

    @NotNull
    public static HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder(settings).getHomeomorphism(testCase, timeout, "SYSTEMTEST", true);
    }

}
