package system;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.GraphGenerator;
import com.charrey.graph.generation.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpecialTest extends SystemTest {

    private static final String patternDOT = "strict graph G {\n" +
            "  0;\n" +
            "  1;\n" +
            "  2;\n" +
            "  1 -- 0;\n" +
            "  1 -- 2;\n" +
            "  0 -- 2;\n" +
            "}\n";

    private static final String targetDOT = "strict graph G {\n" +
            "  0;\n" +
            "  1;\n" +
            "  2;\n" +
            "  3;\n" +
            "  4;\n" +
            "  5;\n" +
            "  6;\n" +
            "  2 -- 1;\n" +
            "  1 -- 0;\n" +
            "  3 -- 0;\n" +
            "  4 -- 3;\n" +
            "  2 -- 4;\n" +
            "}\n";


    @Test
    public void specialTest() throws IOException {
        DOTImporter<Vertex, DefaultEdge> importer = new DOTImporter<>();
        Logger.getLogger("IsoFinder").setLevel(Level.ALL);
        Graph<Vertex, DefaultEdge> patternGraph = new SimpleGraph<>(new GraphGenerator.IntGenerator(), DefaultEdge::new, false);
        importer.importGraph(patternGraph, new StringReader(patternDOT));

        Graph<Vertex, DefaultEdge> targetGraph = new SimpleGraph<>(new GraphGenerator.IntGenerator(), DefaultEdge::new, false);
        importer.importGraph(targetGraph, new StringReader(targetDOT));

        this.testSucceed(new TestCase(new GraphGeneration(patternGraph, null), new GraphGeneration(targetGraph, null)), false);
    }
}
