package scriptie;

import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.random.TrulyRandomDirectedTestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator2;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class ContractionPerformance extends SystemTest {

    private static List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting()              .withContraction().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting()         .withContraction().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       , new SettingsBuilder().withControlPointRouting()       .withContraction().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withContraction().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withContraction().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting()    .withContraction().get()));
    }

    @Test
    public void testLittleBigger() throws InterruptedException { //crashes x = 6 case 816 CP
        run(3.0, 1.5, 4.0);
    }

    @Test
    public void testMuchBigger() throws InterruptedException { //crashes x = 7 case 445 CP
        run(3.0, 5.0, 4.0);
    }



    void run(double sourceDegree, double sizeFactor, double targetdegree) throws InterruptedException {
        Random random = new Random(512);
        long timeout = 10*60*1000L;
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();
                List<Double> stdevs = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + ", cases done = " + lastCasesDone);
                    long timeStartForThisX = System.currentTimeMillis();
                    List<Long> times = new ArrayList<>();
                    int cases = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout) {
                        cases++;
                        long testcaseSeed = random.nextLong();
                        TestCase tc = getTestCase(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed);
                        if (cases < 0) {
                            continue;
                        }
                        long startTime = System.nanoTime();
                        HomeomorphismResult result;
                        try {
                            result = testWithoutExpectation(tc, timeout, configuration.getSettings());
                        } catch (Exception | Error e) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            throw e;
                        }
                        long period = System.nanoTime() - startTime;
                        if (result instanceof FailResult) {
                            System.out.println(configuration.toString() + " failed, case="+cases);
                            assert false;
                        } else if (result instanceof SuccessResult) {
                            times.add(period);
                        }
                    }
                    if (!times.isEmpty()) {
                        results.add(times.stream().mapToDouble(y -> y / 1_000_000_000d).average().orElse(-1));
                        double stdevFoundTime = new StandardDeviation().evaluate(times.stream().mapToDouble(y -> y / 1_000_000_000d).toArray());
                        stdevs.add(stdevFoundTime);
                        x.add(currentX);
                    }
                    lastCasesDone = times.size();
                    currentX++;
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

    private TestCase getTestCase(int vs, int es, int vt, int et, long seed) {
        RandomSucceedDirectedTestCaseGenerator2 gen = new RandomSucceedDirectedTestCaseGenerator2(vs, es, vt, et, seed);
        gen.init(1);
        return gen.getNext();
    }
}
