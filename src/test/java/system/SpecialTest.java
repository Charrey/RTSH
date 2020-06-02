package system;

import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.TestCase;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpecialTest extends SystemTest {

    private static final String patternDOT = "strict digraph G {  0;  1;  2;  2 -> 1;  2 -> 0;  1 -> 2;}\n";

    private static final String targetDOT = "strict digraph G {  0;  1;  2;  3;  4;  5;  2 -> 1;  2 -> 0;  3 -> 2;  1 -> 3;  1 -> 4;  3 -> 4;  2 -> 4;}\n";


    @Test
    public void specialTest() throws IOException {
        Logger.getLogger("IsoFinder").setLevel(Level.ALL);
        DOTImporter<Vertex, DefaultEdge> importer = new DOTImporter<>();
        MyGraph patternGraph = new MyGraph(false);
        importer.importGraph(patternGraph, new StringReader(patternDOT));
        MyGraph targetGraph = new MyGraph(false);
        importer.importGraph(targetGraph, new StringReader(targetDOT));
        this.testSucceed(new TestCase(patternGraph, targetGraph), false, 3600_000);
    }
}
