package unit;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class ControlPointIteratorTest {

    String targetDOT = "strict graph G {\n" +
            "  0--1;\n" +
            "  1--2;\n" +
            "  2--3;\n" +
            "  3--4;\n" +
            "  0--5;\n" +
            "  5--6;\n" +
            "  1--6;\n" +
            "  6--7;\n" +
            "  2--7;\n" +
            "  7--8;\n" +
            "  3--8;\n" +
            "  8--9;\n" +
            "  4--9;\n" +
            "  5--10;\n" +
            "  10--11;\n" +
            "  6--11;\n" +
            "  11--12;\n" +
            "  7--12;\n" +
            "  12--13;\n" +
            "  8--13;\n" +
            "  13--14;\n" +
            "  9--14;\n" +
            "  10--15;\n" +
            "  15--16;\n" +
            "  11--16;\n" +
            "  16--17;\n" +
            "  12--17;\n" +
            "  17--18;\n" +
            "  13--18;\n" +
            "  18--19;\n" +
            "  14--19;\n" +
            "}\n";

    @Test
    public void test() {
        DOTImporter<Vertex, DefaultEdge> importer = new DOTImporter<>();
        MyGraph graph = new MyGraph(false);
        importer.importGraph(graph, new StringReader(targetDOT));
        Occupation occupation = new Occupation(null, graph.vertexSet().size());
        Vertex tail = graph.vertexSet().stream().filter(x -> x.data() == 19).findAny().get();
        Vertex head = graph.vertexSet().stream().filter(x -> x.data() == 0).findAny().get();
        ManagedControlPointIterator cpIterator = new ManagedControlPointIterator(graph, tail, head, occupation, 10);
        Path path = cpIterator.next();
        while (path!=null) {
            System.out.println(path);
            path = cpIterator.next();
        }
    }


}
