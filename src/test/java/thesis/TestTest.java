package thesis;

import com.charrey.Configuration;
import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static thesis.Util.comparitiveTest;

public class TestTest extends SystemTest  {
    private static final String sourceDOT = "digraph G {\n" +
            "  0 [ label=\"0 [wire]\" ];\n" +
            "  1 [ label=\"1 [port]\" ];\n" +
            "  2 [ label=\"2 [SLICE]\" ];\n" +
            "  3 [ label=\"3 [wire]\" ];\n" +
            "  4 [ label=\"4 [port]\" ];\n" +
            "  0 -> 1;\n" +
            "  1 -> 2;\n" +
            "  2 -> 4;\n" +
            "  4 -> 0;\n" +
            "}\n";
    private static final String targetDOT = "digraph G {\n" +
            "  0 [ label=\"0 [arc]\" ];\n" +
            "  1 [ label=\"1 [wire]\" ];\n" +
            "  2 [ label=\"2 [port]\" ];\n" +
            "  3 [ label=\"3 [wire]\" ];\n" +
            "  4 [ label=\"4 [arc]\" ];\n" +
            "  5 [ label=\"5 [SLICE]\" ];\n" +
            "  6 [ label=\"6 [port]\" ];\n" +
            "  7 [ label=\"7 [CE, port]\" ];\n" +
            "  8 [ label=\"8 [arc]\" ];\n" +
            "  9 [ label=\"9 [arc]\" ];\n" +
            "  10 [ label=\"10 [arc]\" ];\n" +
            "  1 -> 0;\n" +
            "  2 -> 1;\n" +
            "  0 -> 3;\n" +
            "  3 -> 4;\n" +
            "  4 -> 1;\n" +
            "  5 -> 2;\n" +
            "  1 -> 6;\n" +
            "  6 -> 5;\n" +
            "  1 -> 7;\n" +
            "  7 -> 5;\n" +
            "  1 -> 8;\n" +
            "  8 -> 3;\n" +
            "  3 -> 9;\n" +
            "  9 -> 1;\n" +
            "  3 -> 10;\n" +
            "  10 -> 1;\n" +
            "}\n";

    @Test
    public void testTest() { //cr
        Settings settings = new SettingsBuilder()
                .withContraction().withControlPointRouting().allowingLongerPaths().withoutPruning().get();
        MyGraph sourceGraph = importGraph(sourceDOT);
        MyGraph targetGraph = importGraph(targetDOT);
        TestCase tc = new TestCase(sourceGraph, targetGraph, null, null);
        HomeomorphismResult result = SystemTest.testWithoutExpectation(tc, 10000*1000, settings);
        assert result instanceof SuccessResult;
        System.out.println(result);
    }

    private MyGraph importGraph(String dot) {
        MyGraph res = new MyGraph(true);
        DOTImporter<Integer, MyEdge> importer = new DOTImporter<>();
        importer.addVertexAttributeConsumer(new BiConsumer<Pair<Integer, String>, Attribute>() {
            @Override
            public void accept(Pair<Integer, String> integerStringPair, Attribute attribute) {
                if (integerStringPair.getSecond().equals("label")) {
                    String[] attributes = attribute.getValue().split("\\[")[1].split("]")[0].split(", ");
                    for (String s : attributes) {
                        res.addAttribute(integerStringPair.getFirst(), "label", s);
                    }
                }
            }
        });
        importer.importGraph(res, new StringReader(dot));
        return res;
    }


}
