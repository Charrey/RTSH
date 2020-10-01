package scriptie;

                                                                                                                                                                                                                                                import com.charrey.Configuration;
                                                                                                                                                                                                                                                import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.random.ScriptieFailDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
                                                                                                                                                                                                                                                import com.charrey.result.SuccessResult;
                                                                                                                                                                                                                                                import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.text.SimpleDateFormat;
import java.util.*;

public class PathIteratorOverhead extends SystemTest {



    @Test
    public void testSmallLabels() throws InterruptedException {
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*"       , "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting().withoutContraction().get()));
        configurations.add(new Configuration("x"       , "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("+"       , "green" , "CP"       , new SettingsBuilder().withControlPointRouting().withoutContraction().get()));
        configurations.add(new Configuration("o"       , "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "black", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star"    , "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting().withoutContraction().get()));
        run(configurations);
    }

    private final Portfolio folio = new Portfolio();

    void run(List<Configuration> configurations) throws InterruptedException {
        long timeout = 10*60*1000L;
        Map<Configuration, Thread> threads = new HashMap<>();
        for (Configuration configuration : configurations) {
            Thread theThread = new Thread(() -> {
                Random threadRandom = new Random(1293148);
                List<Integer> x = new ArrayList<>();
                List<Double> results = new ArrayList<>();
                int currentX = 4;
                int lastCasesDone = 10;
                while (lastCasesDone > 1) {
                    Random xRandom = new Random(threadRandom.nextLong());
                    System.out.println(configuration + ", x = " + currentX + ", last cases = " + lastCasesDone + ", timestamp=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
                    long timeStartForThisX = System.currentTimeMillis();
                    List<Long> times = new ArrayList<>();
                    long tempTimeout = timeout;
                    while (System.currentTimeMillis() - timeStartForThisX < tempTimeout && times.size() < 200) {
                        TestCase tc = getTestCase(xRandom, currentX);
                        long startTime = System.nanoTime();
                        HomeomorphismResult result = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                        long period = System.nanoTime() - startTime;
                        if (result instanceof FailResult) {
                            times.add(period);
                            folio.register(currentX, times.size() - 1, period, true);
                        } else if (result instanceof SuccessResult) {
                            tempTimeout += period / 1_000_000d;
                        }
                    }
                    if (!times.isEmpty()) {
                        double toAdd = times.stream().mapToDouble(y -> y / 1_000_000_000d).average().orElse(-1);
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
        System.out.println(folio.toString());
    }

    private TestCase getTestCase(Random xRandom, int currentX) {
        TestCaseGenerator gen = new ScriptieFailDirectedTestCaseGenerator(currentX, 1.5, xRandom.nextInt()).init(1);
        return gen.getNext();
    }

//    private TestCase getTestCase(Random random, int sourceGraphNodes, boolean labels) {
//        int patternNodes = sourceGraphNodes;
//        int patternEdges = Math.toIntExact(Math.round(patternNodes * 2.429));
//        TestCaseGenerator gen = new TrulyRandomDirectedTestCaseGenerator(patternNodes, patternEdges, 1, random.nextInt()).init(1);
//        MyGraph sourceGraph = gen.getNext().getSourceGraph();
//        patternNodes = Math.toIntExact(Math.round(patternNodes * 1.5));
//        patternEdges = Math.toIntExact(Math.round(patternNodes * 3.425));
//        gen = new TrulyRandomDirectedTestCaseGenerator(patternNodes, patternEdges, 1, random.nextInt()).init(1);
//        MyGraph targetGraph = gen.getNext().getSourceGraph();
//        if (labels) {
//            int sourceWires = (int) Math.max(1, Math.round(9d * sourceGraph.vertexSet().size() / 14d));
//            int sourceSlices = (int) Math.max(1, Math.round(sourceGraph.vertexSet().size() / 14d));
//            List<Integer> vertices = new ArrayList<>(sourceGraph.vertexSet());
//            Collections.shuffle(vertices, random);
//            for (int i = 0; i < sourceWires; i++) {
//                sourceGraph.addAttribute(vertices.get(i), "label", "wire");
//            }
//            for (int i = sourceWires; i < sourceWires + sourceSlices; i++) {
//                sourceGraph.addAttribute(vertices.get(i), "label", "SLICE");
//            }
//            for (int i = sourceWires + sourceSlices; i < vertices.size(); i++) {
//                sourceGraph.addAttribute(vertices.get(i), "label", "arc");
//            }
//            int targetWires = (int) Math.max(sourceWires, Math.round(414d * targetGraph.vertexSet().size() / 2908d));
//            int targetPorts = targetWires + (int) Math.round(124d * targetGraph.vertexSet().size() / 2908d);
//            int targetSlices = targetPorts + (int) Math.max(sourceSlices, Math.round(4d * targetGraph.vertexSet().size() / 2908d));
//            vertices = new ArrayList<>(targetGraph.vertexSet());
//            Collections.shuffle(vertices, random);
//            for (int i = 0; i < targetWires; i++) {
//                targetGraph.addAttribute(vertices.get(i), "label", "wire");
//            }
//            for (int i = targetWires; i < targetPorts; i++) {
//                targetGraph.addAttribute(vertices.get(i), "label", "port");
//            }
//            for (int i = targetPorts; i < targetSlices; i++) {
//                targetGraph.addAttribute(vertices.get(i), "label", "SLICE");
//            }
//            for (int i = targetSlices; i < vertices.size(); i++) {
//                targetGraph.addAttribute(vertices.get(i), "label", "arc");
//            }
//        }
//        return new TestCase(sourceGraph, targetGraph, null, null);
//    }



}
