package unit;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.iterator.ControlPointIteratorStrategy;
import com.charrey.settings.iterator.GreedyDFSStrategy;
import com.charrey.settings.iterator.IteratorSettings;
import com.charrey.settings.iterator.KPathStrategy;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class BigGraphTest {


    private MyGraph targetGraph;

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

    private Set<Integer> matchFrom() {
        return targetGraph.vertexSet().stream().filter(vertex -> targetGraph.getLabels(vertex).iterator().next().startsWith("matchfrom")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Integer> matchTo() {
        return targetGraph.vertexSet().stream().filter(vertex -> targetGraph.getLabels(vertex).iterator().next().startsWith("matchto")).collect(Collectors.toUnmodifiableSet());
    }


    private static volatile boolean failed = false;

    private static Thread getThread(TestCase testCase, IteratorSettings strategy) {
        return new Thread(() -> {
            Settings settings = new SettingsBuilder()
                    .withZeroDomainPruning()
                    .withPathIteration(strategy)
                    .withParallelPruning()
                    .withVertexLimit(20).get();
            try {
                HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase.copy(), settings, 60 * 60 * 1000, strategy.toString());
                System.out.println(result);
                System.out.println(strategy.toString() + " IS FINISHED ------------------------------------");
                System.out.flush();
                if (result instanceof SuccessResult) {
                    System.exit(0);
                }
            } catch (Throwable e) {
                failed = true;
                throw e;
            }
        });
    }

    private static MyGraph getSourceGraph() {
        MyGraph sourceGraph = new MyGraph(true);
        int northIn = sourceGraph.addVertex("label", "wire");
        int northOut = sourceGraph.addVertex("label", "wire");
        int southIn = sourceGraph.addVertex("label", "wire");
        int southOut = sourceGraph.addVertex("label", "wire");
        int westIn = sourceGraph.addVertex("label", "wire");
        int westOut = sourceGraph.addVertex("label", "wire");
        int eastIn = sourceGraph.addVertex("label", "wire");
        int eastOut = sourceGraph.addVertex("label", "wire");

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
        //sourceGraph.addEdge(clockEnableArcWest, clockEnable);
        //sourceGraph.addEdge(clockEnableArcEast, clockEnable);
        //sourceGraph.addEdge(clockEnable, lut);

        return sourceGraph;

    }

    @Test
    @Disabled
    void importSingleTile() throws IOException, InterruptedException {
        targetGraph = new MyGraph(true);
        importDOT(targetGraph, new File("C:\\Users\\Pim van Leeuwen\\VirtualBox VMs\\Afstuderen Backup\\Shared folder\\singleTile.dot"));
        removeTails();
        targetGraph.randomizeWeights();

        matchFrom().forEach(v -> targetGraph.addAttribute(v, "label", "matchfrom"));
        matchTo().forEach(v -> targetGraph.addAttribute(v, "label", "matchto"));

        MyGraph sourceGraph = getSourceGraph();
        TestCase testCase = new TestCase(sourceGraph, targetGraph);

        failed = false;
        Thread threadKPath = getThread(testCase, new KPathStrategy());
        Thread threadDFSArbitrary = getThread(testCase, new GreedyDFSStrategy());
        Thread threadDFSGreedy = getThread(testCase, new GreedyDFSStrategy());
        Thread threadControlPoint = getThread(testCase, new ControlPointIteratorStrategy(0));
        //threadKPath.start();
        //threadDFSArbitrary.start();
        //threadDFSGreedy.start();
        threadControlPoint.start();
        //threadKPath.join();
        //threadDFSArbitrary.join();
        //threadDFSGreedy.join();
        threadControlPoint.join();
        assert !failed;
    }

    private void removeTails() {
        boolean done = false;

        while (!done) {
            done = true;
            for (int v : new HashSet<>(targetGraph.vertexSet())) {
                if (targetGraph.inDegreeOf(v) == 0 || targetGraph.outDegreeOf(v) == 0) {
                    assert targetGraph.getLabels(v).size() == 1;
                    String label = targetGraph.getLabels(v).iterator().next();
                    switch (label) {
                        case "arc":
                        case "port":
                        case "wire":
                            done = false;
                            targetGraph.removeVertex(v);
                            continue;
                        default:
                            assert label.startsWith("matchfrom") || label.startsWith("matchto");
                    }
                }
            }
        }
        targetGraph = GraphUtil.repairVertices(targetGraph);
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
}
