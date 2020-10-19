package thesis;

import com.charrey.Configuration;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class PathIteratorPerformance extends SystemTest {

    private static List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting()              .withoutContraction().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting()         .withoutContraction().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       , new SettingsBuilder().withControlPointRouting()       .withoutContraction().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting()    .withoutContraction().get()));
    }

    @Test
    public void test1() throws InterruptedException {
        run(1.5);
    }

    @Test
    public void test2() throws InterruptedException {
        run(3);
    }

    @Test
    public void test3() throws InterruptedException {
        run(5);
    }

    @Test
    public void test4() throws InterruptedException {
        run(15);
    }

    @Test
    public void test5() throws InterruptedException {
        run(100);
    }

    void run(double factor) throws InterruptedException {
        long timeout = 10*60*1000L;
        Portfolio portfolio = new Portfolio();
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(512);
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random xRandom = new Random(threadRandom.nextLong());
                    System.out.println(configuration + ", x = " + currentX + ", last cases = " + lastCasesDone + ", timestamp=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
                    long timeStartForThisX = System.currentTimeMillis();
                    List<Long> times = new ArrayList<>();
                    while (System.currentTimeMillis() - timeStartForThisX < timeout && times.size() < 200) {
                        TestCase tc = getTestCase(currentX, factor, xRandom.nextInt());
                        long startTime = System.nanoTime();
                        HomeomorphismResult result = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                        long period = System.nanoTime() - startTime;
                        if (result instanceof FailResult) {
                            System.out.println("bug in " + configuration + " with source " + tc.getSourceGraph() + " and target " + tc.getTargetGraph());
                        } else if (result instanceof SuccessResult) {
                            times.add(period);
                            portfolio.register(currentX, times.size() - 1, period, true);
                        }
                    }
                    if (!times.isEmpty()) {
                        double toAdd = times.stream().mapToDouble(y -> y / 1_000_000_000d).average().orElse(-1);
                        System.out.println(configuration + ": " + toAdd);
                        results.add(toAdd);
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
        System.out.println(portfolio.toString());
    }

    private TestCase getTestCase(int vs, double factor, int seed) {
        ScriptieSucceedDirectedTestCaseGenerator gen = new ScriptieSucceedDirectedTestCaseGenerator(vs, factor, seed);
        gen.init(1);
        return gen.getNext();
    }


}
