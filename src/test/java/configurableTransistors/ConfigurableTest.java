package configurableTransistors;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.settings.PruningConstants;
import com.charrey.settings.Settings;
import com.charrey.settings.iteratorspecific.KPathStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ConfigurableTest {

    @Test
    public void testNormal() {
        MyGraph sourceGraph = getSourceGraph();
        MyGraph targetGraph = new MyGraph(true);
        int topLeft = targetGraph.addVertex();
        int topMiddleLeft = targetGraph.addVertex();
        int topMiddle = targetGraph.addVertex();
        int topMiddleRight = targetGraph.addVertex();
        int topRight = targetGraph.addVertex();
        //int bottomLeft = targetGraph.addVertex();
        //int bottomMiddleLeft = targetGraph.addVertex();
        //int bottomMiddle = targetGraph.addVertex();
        //int bottomMiddleRight = targetGraph.addVertex();
        //int bottomRight = targetGraph.addVertex();
        targetGraph.addAttribute(topLeft, "label", "topleft");
        targetGraph.addAttribute(topMiddleLeft, "label", "arc");
        targetGraph.addAttribute(topMiddle, "label", "wire");
        targetGraph.addAttribute(topMiddleRight, "label", "arc");
        targetGraph.addAttribute(topRight, "label", "topright");
        //targetGraph.addAttribute(bottomLeft, "label", "bottomleft");
        //targetGraph.addAttribute(bottomMiddleLeft, "label", "arc");
        //targetGraph.addAttribute(bottomMiddle, "label", "wire");
        //targetGraph.addAttribute(bottomMiddleRight, "label", "arc");
        //targetGraph.addAttribute(bottomRight, "label", "bottomright");
        targetGraph.addEdge(topLeft, topMiddleLeft);
        targetGraph.addEdge(topMiddleLeft, topMiddle);
        targetGraph.addEdge(topMiddle, topMiddleRight);
        targetGraph.addEdge(topMiddleRight, topRight);
        //targetGraph.addEdge(bottomLeft, bottomMiddleLeft);
        //targetGraph.addEdge(bottomMiddleLeft, bottomMiddle);
        //targetGraph.addEdge(bottomMiddle, bottomMiddleRight);
        //targetGraph.addEdge(bottomMiddleRight, bottomRight);
        System.out.println(sourceGraph);
        System.out.println(targetGraph);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(new TestCase(sourceGraph, targetGraph), new Settings(true, true, true, PruningConstants.NONE, new KPathStrategy()), 10 * 60 * 1000, "foo");
        System.out.println(result);
        assertArrayEquals(new int[]{0, 4, 5, 9}, result.getPlacement());
    }


    private MyGraph getSourceGraph() {
        MyGraph sourceGraph = new MyGraph(true);
        int topLeft = sourceGraph.addVertex();
        int topMiddle = sourceGraph.addVertex();
        int topRight = sourceGraph.addVertex();
        //int bottomLeft = sourceGraph.addVertex();
        //int bottomMiddle = sourceGraph.addVertex();
        //int bottomRight = sourceGraph.addVertex();
        sourceGraph.addAttribute(topLeft, "label", "topleft");
        sourceGraph.addAttribute(topMiddle, "label", "arc");
        sourceGraph.addAttribute(topRight, "label", "topright");
        //sourceGraph.addAttribute(bottomLeft, "label", "bottomleft");
        //sourceGraph.addAttribute(bottomMiddle, "label", "arc");
        //sourceGraph.addAttribute(bottomRight, "label", "bottomright");
        sourceGraph.addEdge(topLeft, topMiddle);
        sourceGraph.addEdge(topMiddle, topRight);
        //sourceGraph.addEdge(bottomLeft, bottomMiddle);
        //sourceGraph.addEdge(bottomMiddle, bottomRight);
        return sourceGraph;
    }
}
