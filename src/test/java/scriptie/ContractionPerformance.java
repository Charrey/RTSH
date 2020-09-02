package scriptie;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator2;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class ContractionPerformance extends SystemTest {

    private static final List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting()              .withContraction().get(),  new SettingsBuilder().withKPathRouting()              .withoutContraction().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting()         .withContraction().get(), new SettingsBuilder().withInplaceDFSRouting()         .withoutContraction().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       , new SettingsBuilder().withControlPointRouting()       .withContraction().get(), new SettingsBuilder().withControlPointRouting()       .withoutContraction().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withContraction().get(), new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withContraction().get(), new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting()    .withContraction().get(),new SettingsBuilder().withCachedGreedyDFSRouting()    .withoutContraction().get()));
    }

    @Test
    public void testLittleBigger() throws InterruptedException { //crashes x = 6 case 816 CP
        directedLatexTest(configurations, 3.0, 1.5, 4.0);
    }

    @Test
    public void testMuchBigger() throws InterruptedException { //crashes x = 7 case 445 CP
        directedLatexTest(configurations, 3.0, 5.0, 4.0);
    }



    static void directedLatexTest(List<Configuration> configurations, double sourceDegree, double sizeFactor, double targetdegree) throws InterruptedException {
        long timeout = 30*60*1000L;
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random random = new Random(512);
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();

                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + ", cases done = " + lastCasesDone);
                    long timeStartForThisX = System.currentTimeMillis();
                    List<Double> times = new ArrayList<>();
                    int cases = 0;
                    while (System.currentTimeMillis() - timeStartForThisX < timeout) {
                        cases++;
                        long testcaseSeed = random.nextLong();
                        TestCase tc = getTestCase(currentX, (int) Math.round(currentX * sourceDegree), (int)Math.round(currentX * sizeFactor), (int) Math.round(currentX * sizeFactor * targetdegree), testcaseSeed);
                        if (cases < 0) {
                            continue;
                        }
                        HomeomorphismResult resultWith = null;
                        HomeomorphismResult resultWithout = null;
                        double periodWith;
                        double periodWithout = 0;
                        try {
                            long startTime = System.nanoTime();
                            resultWith = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                            periodWith = System.nanoTime() - startTime;
                            if (configuration.getSettingsWithoutContraction() != null) {
                                startTime = System.nanoTime();
                                resultWithout = testWithoutExpectation(tc, timeout, configuration.getSettingsWithoutContraction());
                                periodWithout = System.nanoTime() - startTime;
                            }
                        } catch (Exception | Error e) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                            e.printStackTrace();
                            continue;
                        }
                        if (resultWithout instanceof FailResult || resultWith instanceof FailResult) {
                            System.out.println(configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                        } else if (resultWith instanceof SuccessResult && (configuration.getSettingsWithoutContraction() == null || resultWithout instanceof SuccessResult)) {
                            times.add((periodWith / periodWithout) - 1.0);
                        }
                    }
                    if (!times.isEmpty()) {
                        results.add(times.stream().mapToDouble(y -> y / 1_000_000_000d).average().orElse(-1));
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

    private static TestCase getTestCase(int vs, int es, int vt, int et, long seed) {
        RandomSucceedDirectedTestCaseGenerator2 gen = new RandomSucceedDirectedTestCaseGenerator2(vs, es, vt, et, seed);
        gen.init(1);
        return gen.getNext();
    }
}
