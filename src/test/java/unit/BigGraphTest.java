package unit;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.settings.PathIterationStrategy;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class BigGraphTest {


    private MyGraph targetGraph;

    private Set<Vertex> wires() {
        return targetGraph.vertexSet().stream().filter(x -> x.getLabels().contains("wire")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Vertex> arcs() {
        return targetGraph.vertexSet().stream().filter(x -> x.getLabels().contains("arc")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Vertex> ports() {
        return targetGraph.vertexSet().stream().filter(x -> x.getLabels().contains("port")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Vertex> bels() {
        return targetGraph.vertexSet().stream().filter(vertex -> {
            String label = vertex.getLabels().iterator().next();
            return !Set.of("wire", "port", "arc").contains(label) && !label.startsWith("match");
        }).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Vertex> matchFrom() {
        return targetGraph.vertexSet().stream().filter(vertex -> vertex.getLabels().iterator().next().startsWith("matchfrom")).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Vertex> matchTo() {
        return targetGraph.vertexSet().stream().filter(vertex -> vertex.getLabels().iterator().next().startsWith("matchto")).collect(Collectors.toUnmodifiableSet());
    }


    @Test
    void importSingleTile() throws IOException, InterruptedException {
        targetGraph = new MyGraph(true);
        importDOT(targetGraph, new File("C:\\Users\\Pim van Leeuwen\\VirtualBox VMs\\Afstuderen Backup\\Shared folder\\singleTile.dot"));
        removeTails(targetGraph);
        for (Vertex v : matchFrom()) {
            v.addAttribute("label", "matchfrom");
        }
        for (Vertex v : matchTo()) {
            v.addAttribute("label", "matchto");
        }
        MyGraph sourceGraph = new MyGraph(true);
        for (int i = 0; i < 3; i++) {
            Vertex vertex1 = sourceGraph.addVertex();
            vertex1.addAttribute("label", "matchfrom");
            Vertex vertex2 = sourceGraph.addVertex();
            vertex2.addAttribute("label", "matchto");
            sourceGraph.addEdge(vertex1, vertex2);
        }


        TestCase testCase = new TestCase(sourceGraph, targetGraph);
        Thread thread1 = new Thread(() -> {
            Settings settings = new Settings(true, true, true, RunTimeCheck.NONE, PathIterationStrategy.YEN, new Random(1234));
            HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 60*60*1000);
            System.out.println(result);
            System.out.println("YEN");
            System.out.flush();
        });
        Thread thread2 = new Thread(() -> {
            Settings settings = new Settings(true, true, true, RunTimeCheck.NONE, PathIterationStrategy.DFS_ARBITRARY, new Random(1234));
            HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 60*60*1000);
            System.out.println(result);
            System.out.println("DFS Arbitrary");
            System.out.flush();
        });
        Thread thread3 = new Thread(() -> {
            Settings settings = new Settings(true, true, true, RunTimeCheck.NONE, PathIterationStrategy.DFS_GREEDY, new Random(1234));
            HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 60*60*1000);
            System.out.println(result);
            System.out.println("DFS Greedy");
            System.out.flush();
        });
        Thread thread4 = new Thread(() -> {
            Settings settings = new Settings(true, true, true, RunTimeCheck.NONE, PathIterationStrategy.CONTROL_POINT, new Random(1234));
            HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 60*60*1000);
            System.out.println(result);
            System.out.println("Controlpoint");
            System.out.flush();
        });
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
    }

    private void removeTails(@NotNull MyGraph graph) {
        boolean done = false;

        while (!done) {
            done = true;
            for (Vertex v : new HashSet<>(graph.vertexSet())) {
                if (graph.inDegreeOf(v) == 0 || graph.outDegreeOf(v) == 0) {
                    assert v.getLabels().size() == 1;
                    String label = v.getLabels().iterator().next();
                    switch (label) {
                        case "arc":
                        case "port":
                        case "wire":
                            done = false;
                            graph.removeVertex(v);
                            continue;
                        default:
                            assert label.startsWith("matchfrom") || label.startsWith("matchto");
                    }
                }
            }
        }
    }


    private final Pattern idFinder = Pattern.compile("\t(\\d*)[ ;]");
    private final Pattern parametersPattern = Pattern.compile("\\[.*]");
    private final Pattern parameterPattern = Pattern.compile("([a-zA-Z]*)=\"(.*?)\"");
    private final Pattern edgePatternUndirected = Pattern.compile("(\\d*) -- (\\d*)");
    private final Pattern edgePatternDirected = Pattern.compile("(\\d*) -> (\\d*)");

    private void importDOT(@NotNull MyGraph targetGraph, @NotNull File file) throws IOException {
        Map<Integer, Vertex> vertices = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(targetGraph.isDirected() ? "->" : "--")) {
                Matcher matcher = (targetGraph.isDirected() ? edgePatternDirected : edgePatternUndirected).matcher(line);
                boolean found = matcher.find();
                assert found;
                int from = Integer.parseInt(matcher.group(1));
                int to = Integer.parseInt(matcher.group(2));
                targetGraph.addEdge(vertices.get(from), vertices.get(to));
            } else if (!(line.contains("graph G {") || line.equals("}"))) {
                Matcher matcher = idFinder.matcher(line);
                boolean found = matcher.find();
                assert found;
                String id = matcher.group(1);
                Vertex vertex = new Vertex(Integer.parseInt(id));
                vertices.put(vertex.data(), vertex);
                targetGraph.addVertex(vertex);
                matcher = parametersPattern.matcher(line);
                if (matcher.find()) {
                    String parameters = matcher.group(0);
                    matcher = parameterPattern.matcher(parameters);
                    while (matcher.find()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);
                        vertex.addAttribute(key, value);
                    }
                }
            }
        }
        System.gc();
    }
}
