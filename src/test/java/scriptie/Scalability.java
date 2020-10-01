package scriptie;

import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class Scalability extends SystemTest {


    MyGraph sourceGraph = getSourceGraph();

    @Test
    public void test() throws InterruptedException {
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*"       , "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting().withoutContraction().get()));
        configurations.add(new Configuration("x"       , "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("+"       , "green" , "CP"       , new SettingsBuilder().withControlPointRouting().withoutContraction().get()));
        configurations.add(new Configuration("o"       , "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "black", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star"    , "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting().withoutContraction().get()));
        Portfolio portfolio = new Portfolio();

        long timeout = 10*60*1000L;
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(1293148);
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();
                int currentExtraNodes = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random xRandom = new Random(threadRandom.nextLong());
                    System.out.println(configuration + ", extraNodes = " + currentExtraNodes + ", last cases = " + lastCasesDone + ", timestamp=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
                    long timeStartForThisX = System.currentTimeMillis();
                    List<Long> times = new ArrayList<>();
                    long tempTimeout = timeout;
                    while (System.currentTimeMillis() - timeStartForThisX < tempTimeout && times.size() < 200) {
                        TestCase tc = getTestCase(xRandom, currentExtraNodes, xRandom.nextInt());
                        long startTime = System.nanoTime();
                        HomeomorphismResult result = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                        long period = System.nanoTime() - startTime;
                        if (result instanceof FailResult) {
                            times.add(period);
                            portfolio.register(currentExtraNodes, times.size() - 1, period, true);
                        } else if (result instanceof SuccessResult) {
                            tempTimeout += period / 1_000_000d;
                        }
                    }
                    if (!times.isEmpty()) {
                        double toAdd = times.stream().mapToDouble(y -> y / 1_000_000_000d).average().orElse(-1);
                        results.add(toAdd);
                        x.add(currentExtraNodes);
                    }
                    lastCasesDone = times.size();
                    currentExtraNodes++;
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

    private TestCase getTestCase(Random xRandom, int currentExtraNodes, int seed) {
        return new ScriptieSucceedDirectedTestCaseGenerator(sourceGraph.vertexSet().size(), 0, seed).getRandomWithSourceGraph(sourceGraph);
    }

    private MyGraph getSourceGraph() {
        MyGraph graph = new MyGraph(true);
        int wireIn1 = graph.addVertex();
        graph.addAttribute(wireIn1, "label", "wire");
        int wireIn2 = graph.addVertex();
        graph.addAttribute(wireIn2, "label", "wire");
        int wireIn3 = graph.addVertex();
        graph.addAttribute(wireIn3, "label", "wire");
        int wireIn4 = graph.addVertex();
        graph.addAttribute(wireIn4, "label", "wire");

        int logic1 = graph.addVertex();
        graph.addAttribute(logic1, "label", "SLICE");

        graph.addEdge(wireIn1, logic1);
        graph.addEdge(wireIn2, logic1);
        graph.addEdge(wireIn3, logic1);
        graph.addEdge(wireIn4, logic1);

        int portCe = graph.addVertex();
        graph.addAttribute(portCe, "label", "port");
        graph.addAttribute(portCe, "label", "CE");

        int logic2 = graph.addVertex();
        graph.addAttribute(portCe, "label", "CE");

        graph.addEdge(logic1, portCe);
        graph.addEdge(portCe, logic2);

        graph.addEdge(logic1, logic2);
        graph.addEdge(logic1, logic2);
        graph.addEdge(logic1, logic2);

        int wireOut1 = graph.addVertex();
        graph.addAttribute(wireOut1, "label", "wire");
        int wireOut2 = graph.addVertex();
        graph.addAttribute(wireOut2, "label", "wire");
        int wireOut3 = graph.addVertex();
        graph.addAttribute(wireOut3, "label", "wire");
        int wireOut4 = graph.addVertex();
        graph.addAttribute(wireOut4, "label", "wire");

        graph.addEdge(logic2, wireOut1);
        graph.addEdge(logic2, wireOut2);
        graph.addEdge(logic2, wireOut3);
        graph.addEdge(logic2, wireOut4);

        return graph;
    }
}
