package contraction;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.GraphUtil;
import org.junit.jupiter.api.Test;

public class ContractionTest {

    @Test
    public void circleTest() {
        MyGraph sourceGraph = new MyGraph(true);
        int s1 = sourceGraph.addVertex();
        int s2 = sourceGraph.addVertex();
        int s3 = sourceGraph.addVertex();
        sourceGraph.addAttribute(s1, "label", "red");
        sourceGraph.addEdge(s1, s2);
        sourceGraph.addEdge(s2, s3);
        sourceGraph.addEdge(s3, s1);

        MyGraph targetGraph = new MyGraph(true);
        int t2 = targetGraph.addVertex();
        int t1 = targetGraph.addVertex();
        int t3 = targetGraph.addVertex();
        int t4 = targetGraph.addVertex();

        int t5 = targetGraph.addVertex();
        targetGraph.addAttribute(t1, "label", "red");
        targetGraph.addEdge(t1, t2);
        targetGraph.addEdge(t2, t3);
        targetGraph.addEdge(t3, t4);
        targetGraph.addEdge(t4, t1);

        targetGraph.addEdge(t2, t5);
        targetGraph.addEdge(t5, t2);

        Settings settings = new SettingsBuilder().withContraction().get();
        TestCase testCase = new TestCase(sourceGraph, targetGraph, null, null);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 10*60*1000, "ContractionTest");
        assert result instanceof SuccessResult;
    }

}
