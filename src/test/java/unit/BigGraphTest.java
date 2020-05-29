package unit;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BigGraphTest {


    public MyGraph targetGraph;

    @Test
    public void test() throws IOException, InterruptedException {
        targetGraph = new MyGraph(false);
        System.gc();
        Thread.sleep(100);
        long totalMemory = Runtime.getRuntime().totalMemory();
        System.out.println("memory before import: " + totalMemory);
        importDOT(targetGraph, new File("D:\\VirtualBox VMs\\Afstuderen next attempt\\Shared Folder\\v2.dot"));
        System.gc();
        Thread.sleep(100);
        totalMemory = Runtime.getRuntime().totalMemory();
        System.out.println("memory after import: " + totalMemory);

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
    private void importDOT(MyGraph targetGraph, File file) throws IOException {
        vertices = new HashMap<>(2000000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        long counter = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("--")) {
                Matcher matcher = edgePattern.matcher(line);
                matcher.find();
                int from = Integer.parseInt(matcher.group(1));
                int to = Integer.parseInt(matcher.group(2));
                targetGraph.addEdge(vertices.get(from), vertices.get(to));
            } else if (line.equals("strict graph G {") || line.equals("}")) {
               //do nothing
            } else {
                Matcher matcher = idFinder.matcher(line);
                matcher.find();
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
            if (counter % 100000L == 0L) {
                if (counter < 1_000_000L) {
                    System.out.println(counter / 1000L + "k lines processed...");
                } else {
                    System.out.println(Math.round(100L*counter / 1000000d) / 100. + "m lines processed...");
                }
            }
            counter++;
        }
        vertices = null;
        System.gc();
    }
}
