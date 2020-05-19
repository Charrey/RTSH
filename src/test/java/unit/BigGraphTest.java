package unit;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigGraphTest {


    public SimpleGraph<Vertex, DefaultEdge> targetGraph;

    @Test
    public void test() throws IOException {
        targetGraph = new SimpleGraph<>(new GraphGenerator.IntGenerator(), DefaultEdge::new, false);
        importDOT(targetGraph, new File("D:\\VirtualBox VMs\\Afstuderen next attempt\\Shared Folder\\v2.dot"));
        int vertexSize = targetGraph.vertexSet().size();
        int edgeSize = targetGraph.edgeSet().size();
        int vertexSizeSource = 100;
        int edgeSizeSource = (int) (vertexSizeSource * (edgeSize/ (double) vertexSize));
        double meanPathLength = Math.sqrt(vertexSize - vertexSizeSource);
        double totalPathLength = meanPathLength * edgeSizeSource;
        double kb_per_vertex = 17546.0 / 548313.0;
        double kb_for_paths = totalPathLength * kb_per_vertex;
        System.out.println();

    }



    Pattern idFinder = Pattern.compile("\t(\\d*)[ ;]");
    //Pattern parametersPattern = Pattern.compile("\\[.*\\]");
    //Pattern parameterPattern = Pattern.compile("([a-zA-Z]*)=\"(.*?)\"");
    Pattern edgePattern = Pattern.compile("(\\d*) -- (\\d*)");
    private Map<Integer, Vertex> vertices = new HashMap<>(2000000);
    private void importDOT(SimpleGraph<Vertex, DefaultEdge> targetGraph, File file) throws IOException {
        List<String> lines = new LinkedList<>(Files.readAllLines(file.toPath(),
                Charset.defaultCharset()));
        ListIterator<String> linesIterator = lines.listIterator();
        double totalSize = lines.size();
        int counter = 0;
        double lastPercentage = 0.0;
        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            if (line.contains("--")) {
                Matcher matcher = edgePattern.matcher(line);
                assert matcher.find();
                int from = Integer.parseInt(matcher.group(1));
                int to = Integer.parseInt(matcher.group(2));
                targetGraph.addEdge(vertices.get(from), vertices.get(to));
            } else if (line.equals("strict graph G {") || line.equals("}")) {
               //do nothing
            } else {
                Matcher matcher = idFinder.matcher(line);
                assert matcher.find();
                String id = matcher.group(1);
                Vertex vertex = new Vertex(Integer.parseInt(id));
                vertices.put(vertex.data(), vertex);
                targetGraph.addVertex(vertex);

//                matcher = parametersPattern.matcher(line);
//                if (matcher.find()) {
//                    String parameters = matcher.group(0);
//                    matcher = parameterPattern.matcher(parameters);
//                    while (matcher.find()) {
//                        String key = matcher.group(1);
//                        String value = matcher.group(2);
//                        vertex.addAttribute(key, value);
//                    }
//                }
            }
            linesIterator.remove();
            double newPercentage = 100.0 * counter / totalSize;
            if (newPercentage > lastPercentage + 0.01) {
                System.out.println(newPercentage);
                lastPercentage = newPercentage;
            }
            counter++;
        }
    }
}
