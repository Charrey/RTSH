package unit;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.settings.PruningConstants;
import com.charrey.settings.Settings;
import com.charrey.settings.iteratorspecific.ControlPointIteratorStrategy;
import com.charrey.settings.iteratorspecific.GreedyDFSStrategy;
import com.charrey.settings.iteratorspecific.KPathStrategy;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
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

    @SuppressWarnings("ProhibitedExceptionThrown")
    @Test
    void importSingleTile() throws IOException, InterruptedException {
        targetGraph = new MyGraph(true);
        importDOT(targetGraph, new File("C:\\Users\\Pim van Leeuwen\\VirtualBox VMs\\Afstuderen Backup\\Shared folder\\singleTile.dot"));
        removeTails();
        targetGraph = new GreatestConstrainedFirst().apply(targetGraph);
        targetGraph.randomizeWeights();

        matchFrom().forEach(v -> targetGraph.addAttribute(v, "label", "matchfrom"));
        matchTo().forEach(v -> targetGraph.addAttribute(v, "label", "matchto"));

        MyGraph sourceGraph = new MyGraph(true);
        int startVertex = sourceGraph.addVertex();
        int endVertex = startVertex;
        for (int i = 0; i < 1; i++) {
            int newVertex = sourceGraph.addVertex();
            sourceGraph.addEdge(endVertex, newVertex);
            endVertex = newVertex;
        }
        sourceGraph.addEdge(endVertex, startVertex);

        TestCase testCase = new TestCase(sourceGraph, targetGraph);
        Thread threadKPath = new Thread(() -> {
            Settings settings = new Settings(true, true, true, PruningConstants.ALL_DIFFERENT, new KPathStrategy());
            try {
                HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase.copy(), settings, 60 * 60 * 1000, "KPATH");
                System.out.println(result);
                System.out.println("KPATH IS FINISHED ------------------------------------");
                System.out.flush();
            } catch (Throwable e) {
                failed = true;
                throw e;
            }
        });
        Thread threadDFSArbitrary = new Thread(() -> {
            Settings settings = new Settings(true, true, true, PruningConstants.ALL_DIFFERENT, new GreedyDFSStrategy());
            try {
                HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase.copy(), settings, 60 * 60 * 1000, "DFS");
                System.out.println(result);
                System.out.println("DFS Arbitrary IS FINISHED ------------------------------------");
                System.out.flush();
            } catch (Throwable e) {
                failed = true;
                throw e;
            }
        });
        Thread threadDFSGreedy = new Thread(() -> {
            Settings settings = new Settings(true, true, true, PruningConstants.ALL_DIFFERENT, new GreedyDFSStrategy());
            try {
                HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase.copy(), settings, 60 * 60 * 1000, "DFS GREEDY");
                System.out.println(result);
                System.out.println("DFS Greedy IS FINISHED ------------------------------------");
                System.out.flush();
            } catch (Throwable e) {
                failed = true;
                throw e;
            }
        });
        Thread threadControlPoint = new Thread(() -> {
            Settings settings = new Settings(true, true, true, PruningConstants.ALL_DIFFERENT, new ControlPointIteratorStrategy(0));
            try {
                HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase.copy(), settings, 60 * 60 * 1000, "CP");
                System.out.println(result);
                System.out.println("Controlpoint IS FINISHED ------------------------------------");
                System.out.flush();
            } catch (Throwable e) {
                failed = true;
                throw e;
            }
        });
        threadKPath.start();
        threadDFSArbitrary.start();
        threadDFSGreedy.start();
        threadControlPoint.start();
        threadKPath.join();
        threadDFSArbitrary.join();
        threadDFSGreedy.join();
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
