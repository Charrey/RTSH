package unit;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BigGraphTest {


    private static MyGraph sourceGraph;

    private static volatile boolean failed = false;

    private static BigTestRunnable getThread(TestCase testCase, long timeout) {
        return new BigTestRunnable(testCase, timeout);
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
        runTest("tile1x1.dot");
    }

    @Test
    void test1x2() throws IOException, InterruptedException {
        runTest("tile1x2.dot");
    }

    @Test
    void test2x1() throws IOException, InterruptedException {
        runTest("tile2x1.dot");
    }

    @Test
    void test2x2() throws IOException, InterruptedException {
        runTest("tile2x2.dot");
    }

    @Test
    void test3x3() throws IOException, InterruptedException {
        runTest("tile3x3.dot");
    }


    public static long timeout = (long)  30*60*1000;

    void runTest(String filename) throws IOException, InterruptedException {
        final long initial = System.currentTimeMillis();
        TestCase testCase = getTestCase(filename);
        failed = false;
        final BigTestRunnable runnableWithoutContraction = getThread(testCase, timeout - (System.currentTimeMillis() - initial));
        final Thread threadWithoutContraction = new Thread(runnableWithoutContraction);
        final BigTestRunnable runnableContraction = getThread(testCase, timeout - (System.currentTimeMillis() - initial));
        final Thread threadWithContraction = new Thread(runnableContraction);

        threadWithoutContraction.start();
        threadWithContraction.start();
        threadWithoutContraction.join();
        threadWithContraction.join();
        assertFalse(failed);
    }

    @NotNull
    private TestCase getTestCase(String filename) throws IOException {
        MyGraph targetGraph = new MyGraph(true);
        importDOT(targetGraph, Paths.get(".").resolve("graphs").resolve(filename).toFile());
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
        private final TestCase testCase;
        private final long timeout;

        public BigTestRunnable(TestCase testCase, long timeout) {
            this.testCase = testCase;
            this.timeout = timeout;
        }

        @Override
        public void run() {
                Settings settings = new SettingsBuilder()
                        .withInplaceDFSRouting()
                        .withoutContraction()
                        .withGreatestConstrainedFirstSourceVertexOrder()
                        .withLargestDegreeFirstTargetVertexOrder()
                        .withCachedPruning()
                        .withAllDifferentPruning()
                        .withNeighbourReachabilityFiltering()
                        .get();
                try {
                    HomeomorphismResult result = new IsoFinder(settings).getHomeomorphism(testCase.copy(), timeout, "DFS", false);
                    System.out.println(result);
                    System.out.println("DFS IS FINISHED ------------------------------------");
                    System.out.flush();
                    if (result instanceof SuccessResult) {
                        System.out.println(result);
                        System.out.println("SUCCESS");
                    } else {
                        failed = true;
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
