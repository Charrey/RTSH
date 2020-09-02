package scriptie;

import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.random.TrulyRandomDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.settings.SettingsBuilder;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.util.*;

public class PathIteratorOverhead extends SystemTest {


    private static List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*"       , "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting().withoutContraction().get()));
        configurations.add(new Configuration("x"       , "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("+"       , "green" , "CP"       , new SettingsBuilder().withControlPointRouting().withoutContraction().get()));
        configurations.add(new Configuration("o"       , "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star"    , "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting().withoutContraction().get()));
    }

    @Test
    void run() throws InterruptedException {
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
                    System.out.println(configuration + ", x = " + currentX);
                    long timeStartForThisX = System.currentTimeMillis();
                    List<Long> times = new ArrayList<>();
                    while (System.currentTimeMillis() - timeStartForThisX < timeout) {
                        TestCase tc = getTestCase(random, currentX);
                        long startTime = System.nanoTime();
                        HomeomorphismResult result = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                        long period = System.nanoTime() - startTime;
                        if (result instanceof FailResult) {
                            times.add(period);
                        }
                    }
                    if (!times.isEmpty()) {
                        results.add(times.stream().mapToDouble(y -> y / 1_000_000_000d).average().orElse(-1));
                        double stdevNotFoundTime = new StandardDeviation().evaluate(times.stream().mapToDouble(y -> y / 1_000_000_000d).toArray());
                        stdevs.add(stdevNotFoundTime);
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

    private TestCase getTestCase(Random random, int sourceGraphNodes) {
        int patternNodes = sourceGraphNodes;
        int patternEdges = Math.toIntExact(Math.round(patternNodes * 3.0));
        TrulyRandomDirectedTestCaseGenerator gen = new TrulyRandomDirectedTestCaseGenerator(patternNodes, patternEdges, 1.5, random.nextInt());
        gen.init(1);
        MyGraph sourceGraph = gen.getNext().getSourceGraph();

        patternNodes = Math.toIntExact(Math.round(patternNodes * 1.5));
        patternEdges = Math.toIntExact(Math.round(patternNodes * 4.0));
        gen = new TrulyRandomDirectedTestCaseGenerator(patternNodes, patternEdges, 1.5, random.nextInt());
        gen.init(1);
        MyGraph targetGraph = gen.getNext().getSourceGraph();
        TestCase tc = new TestCase(sourceGraph, targetGraph, null, null);
        return tc;
    }



}
