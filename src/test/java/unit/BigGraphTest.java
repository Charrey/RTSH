package unit;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.iterator.*;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BigGraphTest {


    private MyGraph targetGraph;
    private static MyGraph sourceGraph;

    private Set<Integer> wires() {
        return targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("wire")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Integer> arcs() {
        return targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("arc")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Integer> ports() {
        return targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("port")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Integer> bels() {
        return targetGraph.vertexSet().stream().filter(vertex -> {
            String label = targetGraph.getLabels(vertex).iterator().next();
            return !Set.of("wire", "port", "arc").contains(label) && !label.startsWith("match");
        }).collect(Collectors.toUnmodifiableSet());
    }

//    private Set<Integer> matchFrom() {
//        return targetGraph.vertexSet().stream().filter(vertex -> targetGraph.getLabels(vertex).iterator().next().startsWith("matchfrom")).collect(Collectors.toUnmodifiableSet());
//    }
//
//    private Set<Integer> matchTo() {
//        return targetGraph.vertexSet().stream().filter(vertex -> targetGraph.getLabels(vertex).iterator().next().startsWith("matchto")).collect(Collectors.toUnmodifiableSet());
//    }


    private static volatile boolean failed = false;

    private static BigTestRunnable getThread(TestCase testCase, IteratorSettings strategy, long timeout) {
        return new BigTestRunnable(testCase, strategy, timeout);
    }

    private static MyGraph getSourceGraph() {
        MyGraph sourceGraph = new MyGraph(true);
        int northIn = sourceGraph.addVertex("label", "wire");
        int southIn = sourceGraph.addVertex("label", "wire");
        int westIn = sourceGraph.addVertex("label", "wire");
        int eastIn = sourceGraph.addVertex("label", "wire");

        int lut = sourceGraph.addVertex("label", "SLICE");

        int clockEnableWire = sourceGraph.addVertex("label", "wire");
        int clockEnableArcNorth = sourceGraph.addVertex("label", "arc");
        int clockEnableArcSouth = sourceGraph.addVertex("label", "arc");
        int clockEnableArcEast = sourceGraph.addVertex("label", "arc");
        int clockEnableArcWest = sourceGraph.addVertex("label", "arc");
        sourceGraph.addEdge(northIn, clockEnableArcNorth);
        sourceGraph.addEdge(southIn, clockEnableArcSouth);
        sourceGraph.addEdge(westIn, clockEnableArcWest);
        sourceGraph.addEdge(eastIn, clockEnableArcEast);

        sourceGraph.addEdge(clockEnableArcNorth, clockEnableWire);
        sourceGraph.addEdge(clockEnableArcSouth, clockEnableWire);
        sourceGraph.addEdge(clockEnableArcWest, clockEnableWire);
        sourceGraph.addEdge(clockEnableArcEast, clockEnableWire);

        sourceGraph.addEdge(clockEnableWire, lut);
        sourceGraph.addEdge(northIn, lut);
        sourceGraph.addEdge(westIn, lut);
        sourceGraph.addEdge(eastIn, lut);
        sourceGraph.addEdge(southIn, lut);

        int northOut = sourceGraph.addVertex("label", "wire");
        int southOut = sourceGraph.addVertex("label", "wire");
        int westOut = sourceGraph.addVertex("label", "wire");
        int eastOut = sourceGraph.addVertex("label", "wire");

        sourceGraph.addEdge(lut, northOut);
        sourceGraph.addEdge(lut, southOut);
        sourceGraph.addEdge(lut, eastOut);
        sourceGraph.addEdge(lut, westOut);

        return sourceGraph;

    }


    @BeforeEach
    void init() {
        sourceGraph = getSourceGraph();
    }

    @Test
    void test1x1() throws IOException, InterruptedException {
        runTest("tile1x1.dot", (long) (0.1*60*1000));
    }

    @Test
    void test1x2() throws IOException, InterruptedException {
        runTest("tile1x2.dot", (long) (0.1*60*1000));
    }

    @Test
    void test2x1() throws IOException, InterruptedException {
        runTest("tile2x1.dot", (long) (0.1*60*1000));
    }

    @Test
    void test2x2() throws IOException, InterruptedException {
        runTest("tile2x2.dot", (long) (0.1*60*1000));
    }



    void runTest(String filename, long timeout) throws IOException, InterruptedException {
        final long initial = System.currentTimeMillis();
        TestCase testCase = getTestCase(filename);

        failed = true;
        final BigTestRunnable runnableKPath = getThread(testCase, new KPathStrategy(), timeout - (System.currentTimeMillis() - initial));
        final BigTestRunnable runnableDFSArbitrary = getThread(testCase, new DFSStrategy(), timeout - (System.currentTimeMillis() - initial));
        final BigTestRunnable runnableDFSGreedy = getThread(testCase, new OldGreedyDFSStrategy(), timeout - (System.currentTimeMillis() - initial));
        final BigTestRunnable runnableControlPoint = getThread(testCase, new ControlPointIteratorStrategy(1), timeout - (System.currentTimeMillis() - initial));
        final Thread threadKPath = new Thread(runnableKPath);
        final Thread threadDFSArbitrary = new Thread(runnableDFSArbitrary);
        final Thread threadDFSGreedy = new Thread(runnableDFSGreedy);
        final Thread threadControlPoint = new Thread(runnableControlPoint);
        Runnable onDone = () -> {
            threadKPath.stop();
            threadDFSArbitrary.stop();
            threadDFSGreedy.stop();
            threadControlPoint.stop();
            failed = false;
            System.out.println("FORCE STOPPED");
        };
        runnableKPath.setOnDone(onDone);
        runnableDFSArbitrary.setOnDone(onDone);
        runnableDFSGreedy.setOnDone(onDone);
        runnableControlPoint.setOnDone(onDone);

        threadKPath.start();
        threadDFSArbitrary.start();
        threadDFSGreedy.start();
        threadControlPoint.start();

        threadKPath.join();
        threadDFSArbitrary.join();
        threadDFSGreedy.join();
        threadControlPoint.join();
        assertFalse(failed);
    }

    @NotNull
    private TestCase getTestCase(String filename) throws IOException {
        targetGraph = new MyGraph(true);
        importDOT(targetGraph, Paths.get("/").resolve("home").resolve("pim").resolve("Documents").resolve("Trellis").resolve(filename).toFile());
        targetGraph.randomizeWeights();
        TestCase testCase = new TestCase(sourceGraph, targetGraph, null, null);
        return testCase;
    }


    private final Pattern idFinder = Pattern.compile("\t(\\d*)[ ;]");
    private final Pattern parametersPattern = Pattern.compile("\\[.*]");
    private final Pattern parameterPattern = Pattern.compile("([a-zA-Z]*)=\"(.*?)\"");
    private final Pattern edgePatternUndirected = Pattern.compile("(\\d*) -- (\\d*)");
    private final Pattern edgePatternDirected = Pattern.compile("(\\d*) -> (\\d*)");

    private void importDOT(@NotNull MyGraph targetGraph, @NotNull File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.contains(targetGraph.isDirected() ? "->" : "--")) {
                Matcher matcher = (targetGraph.isDirected() ? edgePatternDirected : edgePatternUndirected).matcher(line);
                boolean found = matcher.find();
                assert found;
                int from = Integer.parseInt(matcher.group(1));
                int to = Integer.parseInt(matcher.group(2));
                targetGraph.addEdge(from, to);
            } else if (!(line.contains("graph G {") || line.equals("}"))) {
                Matcher matcher = idFinder.matcher(line);
                boolean found = matcher.find();
                assert found;
                String id = matcher.group(1);
                int vertex = Integer.parseInt(id);
                targetGraph.addVertex(vertex);
                matcher = parametersPattern.matcher(line);
                if (matcher.find()) {
                    String parameters = matcher.group(0);
                    matcher = parameterPattern.matcher(parameters);
                    while (matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);
                        targetGraph.addAttribute(vertex, key, value);
                    }
                }
            }
        }
        System.gc();
    }

    private static class BigTestRunnable implements Runnable {
        private final IteratorSettings strategy;
        private final TestCase testCase;
        private final long timeout;
        private Runnable onDone;

        public BigTestRunnable(TestCase testCase, IteratorSettings strategy, long timeout) {
            this.strategy = strategy;
            this.testCase = testCase;
            this.timeout = timeout;
        }

        public void setOnDone(Runnable onDone) {
            this.onDone = onDone;
        }

        @Override
        public void run() {
                Settings settings = new SettingsBuilder()
                        .withPathIteration(strategy)
                        .withoutPruning()
                        .withVertexLimit(10)
                        .withPathsLimit(10)
                        //.withClosestTargetVertexOrder()
                        .get();
                try {
                    HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase.copy(), settings, timeout, strategy.toString());
                    System.out.println(result);
                    System.out.println(strategy.toString() + " IS FINISHED ------------------------------------");
                    System.out.flush();
                    if (result instanceof SuccessResult) {
                        onDone.run();
                    }
                } catch (Throwable e) {
                    if (!(e instanceof ThreadDeath)) {
                        e.printStackTrace();
                        failed = true;
                        throw e;
                    }
                }
            }
    }
}
