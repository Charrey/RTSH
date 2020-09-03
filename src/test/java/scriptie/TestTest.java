package scriptie;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator2;
import com.charrey.result.HomeomorphismResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.io.StringReader;

public class TestTest extends SystemTest  {

    private static String sourceString = "digraph G {\n" +
            "  0;\n" +
            "  1;\n" +
            "  2;\n" +
            "  3;\n" +
            "  4;\n" +
            "  2 -> 0;\n" +
            "  4 -> 2;\n" +
            "  3 -> 0;\n" +
            "  2 -> 1;\n" +
            "  0 -> 1;\n" +
            "  4 -> 0;\n" +
            "  1 -> 3;\n" +
            "  4 -> 3;\n" +
            "  4 -> 1;\n" +
            "  1 -> 0;\n" +
            "  2 -> 4;\n" +
            "  2 -> 3;\n" +
            "  1 -> 4;\n" +
            "  0 -> 3;\n" +
            "  0 -> 2;\n" +
            "}";

    private static String targetString = "digraph G {\n" +
            "  0;\n" +
            "  1;\n" +
            "  2;\n" +
            "  3;\n" +
            "  4;\n" +
            "  5;\n" +
            "  6;\n" +
            "  7;\n" +
            "  4 -> 1;\n" +
            "  4 -> 0;\n" +
            "  2 -> 0;\n" +
            "  1 -> 2;\n" +
            "  3 -> 0;\n" +
            "  2 -> 1;\n" +
            "  1 -> 3;\n" +
            "  0 -> 4;\n" +
            "  2 -> 3;\n" +
            "  0 -> 3;\n" +
            "  4 -> 3;\n" +
            "  2 -> 4;\n" +
            "  1 -> 4;\n" +
            "  1 -> 5;\n" +
            "  5 -> 0;\n" +
            "  0 -> 6;\n" +
            "  6 -> 2;\n" +
            "  0 -> 7;\n" +
            "  7 -> 2;\n" +
            "  7 -> 7;\n" +
            "  7 -> 6;\n" +
            "  5 -> 7;\n" +
            "  1 -> 5;\n" +
            "  0 -> 0;\n" +
            "  6 -> 1;\n" +
            "  2 -> 7;\n" +
            "  0 -> 1;\n" +
            "  3 -> 2;\n" +
            "  0 -> 3;\n" +
            "  3 -> 2;\n" +
            "}";

    @Test
    public void test() {
        TestCase tc = new TestCase(getGraph(sourceString), getGraph(targetString), null, null);
        System.out.println(tc);
        Settings settings = new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().get();
        HomeomorphismResult result = testWithoutExpectation(tc, 24*60*60*1000, settings);
        System.out.println(result);
    }

    private MyGraph getGraph(String dot) {
        DOTImporter<Integer, MyEdge> importer = new DOTImporter<>();
        MyGraph res = new MyGraph(true);
        importer.importGraph(res, new StringReader(dot));
        return res;
    }


}
