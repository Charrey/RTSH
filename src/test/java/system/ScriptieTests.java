package system;

import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.random.TrulyRandomDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ScriptieTests extends SystemTest {


    private static List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*", "blue", "K-Path", new SettingsBuilder().withKPathRouting().withoutContraction().get()));
        configurations.add(new Configuration("x", "red", "DFS", new SettingsBuilder().withInplaceDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("+", "green", "CP", new SettingsBuilder().withControlPointRouting().withoutContraction().get()));
        configurations.add(new Configuration("o", "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star", "gray", "GDFS C", new SettingsBuilder().withCachedGreedyDFSRouting().withoutContraction().get()));
    }

    @Test
    public void run() throws InterruptedException {
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
                        HomeomorphismResult result = testWithoutExpectation(tc, timeout, configuration.settings);
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
                System.out.println(configuration.getString(x, results, stdevs));
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


    private static class Configuration {
        private final String prefix;
        private final String suffix;
        private final Settings settings;
        private final String name;

        public Configuration(String mark, String color, String name, Settings settings) {
            this.prefix = "\\addplot[\n" +
                    "        smooth,\n" +
                    "        mark=" + mark + ",\n" +
                    "        " + color + ",\n" +
                    "        error bars/.cd, y dir=both, y explicit,\n" +
                    "    ] plot coordinates {\n";
            this.settings = settings;
            this.suffix = "};\n    \\addlegendentry{" + name + "}\n\n";
            this.name = name;
        }

        public Settings getSettings() {
            return settings;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getString(List<Integer> x, List<Double> results, List<Double> deviations) {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < results.size(); i++) {
                sb.append("        (").append(x.get(i)).append(",").append(results.get(i)).append(") +=(0,").append(deviations.get(i)).append(") -= (0,").append(deviations.get(i)).append(")\n");
            }
            return sb.append(suffix).toString();
        }
    }
}
