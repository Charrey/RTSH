package play;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.util.SupplierUtil;

import java.io.StringReader;

public class Whatever {

    public static void main(String[] args) {
        DOTImporter<Integer, DefaultEdge> importer = new DOTImporter<>();
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(), true);
        importer.addEdgeAttributeConsumer((defaultEdgeStringPair, attribute) -> {
            if (defaultEdgeStringPair.getSecond().equals("weight")) {
                graph.setEdgeWeight(defaultEdgeStringPair.getFirst(), Double.parseDouble(attribute.getValue()));
            }
        });
        importer.importGraph(graph, new StringReader("strict digraph G {\n" +
                "  0;\n" +
                "  1;\n" +
                "  2;\n" +
                "  3;\n" +
                "  4;\n" +
                "  5;\n" +
                "  5 -> 3 [weight=1.0];\n" +
                "  0 -> 5 [weight=1.0000000000000002];\n" +
                "  4 -> 0 [weight=1.0000000000000004];\n" +
                "  1 -> 3 [weight=1.0000000000000007];\n" +
                "  4 -> 2 [weight=1.0000000000000009];\n" +
                "  5 -> 4 [weight=1.000000000000001];\n" +
                "  1 -> 0 [weight=1.0000000000000013];\n" +
                "  3 -> 5 [weight=1.0000000000000016];\n" +
                "  2 -> 3 [weight=1.0000000000000018];\n" +
                "  2 -> 5 [weight=1.000000000000002];\n" +
                "  3 -> 2 [weight=1.0000000000000022];\n" +
                "}\n"));

        YenShortestPathIterator<Integer, DefaultEdge> iterator = new YenShortestPathIterator<>(graph, 0, 2);
        GraphPath<Integer, DefaultEdge> firstFound = iterator.next();
        GraphPath<Integer, DefaultEdge> secondFound = iterator.next();
        assert firstFound.getWeight() <= secondFound.getWeight();
    }
}
