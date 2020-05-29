package jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.EppsteinShortestPathIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.nio.dot.DOTImporter;

import java.io.ByteArrayInputStream;

public class EppsteinTest {

    static String dot = "strict digraph G {  0;  1;  2;  3;  1 -> 0;  1 -> 3;  3 -> 0;}\n" +
            "strict digraph G {\n" +
            "  0;\n" +
            "  1;\n" +
            "  2;\n" +
            "  3;\n" +
            "  3 -> 0;\n" +
            "  1 -> 0;\n" +
            "  1 -> 3;\n" +
            "}";

    public static void main(String[] args) {
        DOTImporter<Integer, DefaultEdge> importer = new DOTImporter<>();
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedWeightedGraph<>(new IntegerGenerator(), DefaultEdge::new);
        importer.importGraph(graph, new ByteArrayInputStream(dot.getBytes()));
        EppsteinShortestPathIterator<Integer, DefaultEdge> iterator = new EppsteinShortestPathIterator<>(graph, 0, 1);

        assert iterator.hasNext();
        System.out.println(iterator.next());
    }

    private static class IntegerGenerator implements java.util.function.Supplier<Integer> {

        int content = 0;

        @Override
        public Integer get() {
            int toReturn = content;
            content++;
            return toReturn;
        }
    }
}
