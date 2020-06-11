package unit;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigGraphTest {


    public MyGraph targetGraph;

    @Test
    public void test() throws IOException {
        targetGraph = new MyGraph(true);
        importDOT(targetGraph, new File("C:\\Users\\Pim van Leeuwen\\VirtualBox VMs\\Afstuderen Backup\\Shared folder\\singleTile.dot"));
        removeTails(targetGraph);
        System.out.println();

    }

    private void removeTails(MyGraph graph) {
        boolean done = false;

        while (!done) {
            done = true;
            for (Vertex v : new HashSet<>(graph.vertexSet())) {
                if (graph.inDegreeOf(v) == 0 || graph.outDegreeOf(v) == 0) {
                    assert v.getLabels().size() == 1;
                    String label = v.getLabels().iterator().next().getValue();
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


    final Pattern idFinder = Pattern.compile("\t(\\d*)[ ;]");
    final Pattern parametersPattern = Pattern.compile("\\[.*]");
    final Pattern parameterPattern = Pattern.compile("([a-zA-Z]*)=\"(.*?)\"");
    final Pattern edgePatternUndirected = Pattern.compile("(\\d*) -- (\\d*)");
    final Pattern edgePatternDirected = Pattern.compile("(\\d*) -> (\\d*)");

    private void importDOT(MyGraph targetGraph, File file) throws IOException {
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
