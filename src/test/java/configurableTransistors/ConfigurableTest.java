package configurableTransistors;

import com.charrey.IsoFinder;
import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurableTest {

    private static final String CONFIGURABLE = "configurable";

    private static MyGraph getSourceGraph() {
        MyGraph sourceGraph = new MyGraph(true);
        int topLeft = sourceGraph.addVertex();
        int topMiddle = sourceGraph.addVertex();
        int topRight = sourceGraph.addVertex();
        int bottomLeft = sourceGraph.addVertex();
        int bottomMiddle = sourceGraph.addVertex();
        int bottomRight = sourceGraph.addVertex();
        sourceGraph.addAttribute(topLeft, "label", "topleft");
        sourceGraph.addAttribute(topMiddle, "label", "arc");
        sourceGraph.addAttribute(topMiddle, CONFIGURABLE, "1");
        sourceGraph.addAttribute(topRight, "label", "topright");
        sourceGraph.addAttribute(bottomLeft, "label", "bottomleft");
        sourceGraph.addAttribute(bottomMiddle, "label", "arc");
        sourceGraph.addAttribute(bottomMiddle, CONFIGURABLE, "1");
        sourceGraph.addAttribute(bottomRight, "label", "bottomright");
        sourceGraph.addEdge(topLeft, topMiddle);
        sourceGraph.addEdge(topMiddle, topRight);
        sourceGraph.addEdge(bottomLeft, bottomMiddle);
        sourceGraph.addEdge(bottomMiddle, bottomRight);
        return sourceGraph;
    }

    @Test
    void testConnectedUnconfigurable() {
        MyGraph sourceGraph = getSourceGraph();
        MyGraph targetGraph = new MyGraph(true);
        int topLeft = targetGraph.addVertex();
        int topMiddleLeft = targetGraph.addVertex();
        int topMiddle = targetGraph.addVertex();
        int topMiddleRight = targetGraph.addVertex();
        int topRight = targetGraph.addVertex();
        int connectorArc = targetGraph.addVertex();
        int bottomLeft = targetGraph.addVertex();
        int bottomMiddleLeft = targetGraph.addVertex();
        int bottomMiddle = targetGraph.addVertex();
        int bottomMiddleRight = targetGraph.addVertex();
        int bottomRight = targetGraph.addVertex();
        targetGraph.addAttribute(topLeft, "label", "topleft");
        targetGraph.addAttribute(topMiddleLeft, "label", "arc");
        targetGraph.addAttribute(topMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(topMiddle, "label", "wire");
        targetGraph.addAttribute(topMiddleRight, "label", "arc");
        targetGraph.addAttribute(topMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(topRight, "label", "topright");
        targetGraph.addAttribute(connectorArc, "label", "arc");
        targetGraph.addAttribute(connectorArc, CONFIGURABLE, "0");
        targetGraph.addAttribute(bottomLeft, "label", "bottomleft");
        targetGraph.addAttribute(bottomMiddleLeft, "label", "arc");
        targetGraph.addAttribute(bottomMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomMiddle, "label", "wire");
        targetGraph.addAttribute(bottomMiddleRight, "label", "arc");
        targetGraph.addAttribute(bottomMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomRight, "label", "bottomright");
        targetGraph.addEdge(topLeft, topMiddleLeft);
        targetGraph.addEdge(topMiddleLeft, topMiddle);
        targetGraph.addEdge(topMiddle, topMiddleRight);
        targetGraph.addEdge(topMiddleRight, topRight);
        targetGraph.addEdge(bottomMiddle, connectorArc);
        targetGraph.addEdge(connectorArc, topMiddle);
        targetGraph.addEdge(bottomLeft, bottomMiddleLeft);
        targetGraph.addEdge(bottomMiddleLeft, bottomMiddle);
        targetGraph.addEdge(bottomMiddle, bottomMiddleRight);
        targetGraph.addEdge(bottomMiddleRight, bottomRight);
        System.out.println(sourceGraph);
        System.out.println(targetGraph);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(
                new TestCase(sourceGraph, targetGraph, null, null),
                new SettingsBuilder().withKPathRouting().withoutContraction().get(),
                10 * 60 * 1000,
                "ConfigurableTest ", false);
        assertFalse(result.succeed);
        System.out.println(result);
    }

    @Test
    void testConnectedSmallSourceUnconfigurable() {
        MyGraph sourceGraph = new MyGraph(true);
        int topLeft = sourceGraph.addVertex();
        int topMiddle = sourceGraph.addVertex();
        int topRight = sourceGraph.addVertex();
        sourceGraph.addAttribute(topLeft, "label", "topleft");
        sourceGraph.addAttribute(topMiddle, "label", "arc");
        sourceGraph.addAttribute(topMiddle, CONFIGURABLE, "1");
        sourceGraph.addAttribute(topRight, "label", "topright");
        sourceGraph.addEdge(topLeft, topMiddle);
        sourceGraph.addEdge(topMiddle, topRight);
        MyGraph targetGraph = new MyGraph(true);
        topLeft = targetGraph.addVertex();
        int topMiddleLeft = targetGraph.addVertex();
        topMiddle = targetGraph.addVertex();
        int topMiddleRight = targetGraph.addVertex();
        topRight = targetGraph.addVertex();
        int connectorArc = targetGraph.addVertex();
        int bottomLeft = targetGraph.addVertex();
        int bottomMiddleLeft = targetGraph.addVertex();
        int bottomMiddle = targetGraph.addVertex();
        int bottomMiddleRight = targetGraph.addVertex();
        int bottomRight = targetGraph.addVertex();
        targetGraph.addAttribute(topLeft, "label", "topleft");
        targetGraph.addAttribute(topMiddleLeft, "label", "arc");
        targetGraph.addAttribute(topMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(topMiddle, "label", "wire");
        targetGraph.addAttribute(topMiddleRight, "label", "arc");
        targetGraph.addAttribute(topMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(topRight, "label", "topright");
        targetGraph.addAttribute(connectorArc, "label", "arc");
        targetGraph.addAttribute(connectorArc, CONFIGURABLE, "0");
        targetGraph.addAttribute(bottomLeft, "label", "bottomleft");
        targetGraph.addAttribute(bottomMiddleLeft, "label", "arc");
        targetGraph.addAttribute(bottomMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomMiddle, "label", "wire");
        targetGraph.addAttribute(bottomMiddleRight, "label", "arc");
        targetGraph.addAttribute(bottomMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomRight, "label", "bottomright");
        targetGraph.addEdge(topLeft, topMiddleLeft);
        targetGraph.addEdge(topMiddleLeft, topMiddle);
        targetGraph.addEdge(topMiddle, topMiddleRight);
        targetGraph.addEdge(topMiddleRight, topRight);
        targetGraph.addEdge(bottomMiddle, connectorArc);
        targetGraph.addEdge(connectorArc, topMiddle);
        targetGraph.addEdge(bottomLeft, bottomMiddleLeft);
        targetGraph.addEdge(bottomMiddleLeft, bottomMiddle);
        targetGraph.addEdge(bottomMiddle, bottomMiddleRight);
        targetGraph.addEdge(bottomMiddleRight, bottomRight);
        System.out.println(sourceGraph);
        System.out.println(targetGraph);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(
                new TestCase(sourceGraph, targetGraph, null, null),
                new SettingsBuilder()
                        .withKPathRouting().withoutContraction().get(),
                10 * 60 * 1000, "ConfigurableTest ", false);
        assertTrue(result.succeed);
        Map<MyEdge, Set<Path>> expected = new HashMap<>();
        expected.put(new MyEdge(0, 1), Util.setOf(new Path(targetGraph, Util.listOf(0, 1))));
        expected.put(new MyEdge(1, 2), Util.setOf(new Path(targetGraph, Util.listOf(1, 2, 3, 4))));
        assertEquals(expected.toString(), ((SuccessResult) result).getEdgePlacement().toString());
    }

    @Test
    void testConnectedConfigurable() {
        MyGraph sourceGraph = getSourceGraph();
        MyGraph targetGraph = new MyGraph(true);
        int topLeft = targetGraph.addVertex();
        int topMiddleLeft = targetGraph.addVertex();
        int topMiddle = targetGraph.addVertex();
        int topMiddleRight = targetGraph.addVertex();
        int topRight = targetGraph.addVertex();
        int connectorArc = targetGraph.addVertex();
        int bottomLeft = targetGraph.addVertex();
        int bottomMiddleLeft = targetGraph.addVertex();
        int bottomMiddle = targetGraph.addVertex();
        int bottomMiddleRight = targetGraph.addVertex();
        int bottomRight = targetGraph.addVertex();
        targetGraph.addAttribute(topLeft, "label", "topleft");
        targetGraph.addAttribute(topMiddleLeft, "label", "arc");
        targetGraph.addAttribute(topMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(topMiddle, "label", "wire");
        targetGraph.addAttribute(topMiddleRight, "label", "arc");
        targetGraph.addAttribute(topMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(topRight, "label", "topright");
        targetGraph.addAttribute(connectorArc, "label", "arc");
        targetGraph.addAttribute(connectorArc, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomLeft, "label", "bottomleft");
        targetGraph.addAttribute(bottomMiddleLeft, "label", "arc");
        targetGraph.addAttribute(bottomMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomMiddle, "label", "wire");
        targetGraph.addAttribute(bottomMiddleRight, "label", "arc");
        targetGraph.addAttribute(bottomMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomRight, "label", "bottomright");
        targetGraph.addEdge(topLeft, topMiddleLeft);
        targetGraph.addEdge(topMiddleLeft, topMiddle);
        targetGraph.addEdge(topMiddle, topMiddleRight);
        targetGraph.addEdge(topMiddleRight, topRight);
        targetGraph.addEdge(bottomMiddle, connectorArc);
        targetGraph.addEdge(connectorArc, topMiddle);
        targetGraph.addEdge(bottomLeft, bottomMiddleLeft);
        targetGraph.addEdge(bottomMiddleLeft, bottomMiddle);
        targetGraph.addEdge(bottomMiddle, bottomMiddleRight);
        targetGraph.addEdge(bottomMiddleRight, bottomRight);
        System.out.println(sourceGraph);
        System.out.println(targetGraph);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(
                new TestCase(sourceGraph, targetGraph, null, null),
                new SettingsBuilder()
                        .withKPathRouting().withoutContraction().get(), 10 * 60 * 1000, "ConfigurableTest ", false);
        assertTrue(result.succeed);
        System.out.println(result);
        assertArrayEquals(new int[]{0, 1, 4, 6, 7, 10}, ((SuccessResult) result).getVertexPlacement());

        Map<MyEdge, Set<Path>> expected = new HashMap<>();
        expected.put(new MyEdge(0, 1), Util.setOf(new Path(targetGraph, Util.listOf(0, 1))));
        expected.put(new MyEdge(3, 4), Util.setOf(new Path(targetGraph, Util.listOf(6, 7))));
        expected.put(new MyEdge(1, 2), Util.setOf(new Path(targetGraph, Util.listOf(1, 2, 3, 4))));
        expected.put(new MyEdge(4, 5), Util.setOf(new Path(targetGraph, Util.listOf(7, 8, 9, 10))));
        assertEquals(expected.toString(), ((SuccessResult) result).getEdgePlacement().toString());
    }

    @Test
    void testDisconnected() {
        MyGraph sourceGraph = getSourceGraph();
        MyGraph targetGraph = new MyGraph(true);
        int topLeft = targetGraph.addVertex();
        int topMiddleLeft = targetGraph.addVertex();
        int topMiddle = targetGraph.addVertex();
        int topMiddleRight = targetGraph.addVertex();
        int topRight = targetGraph.addVertex();
        int bottomLeft = targetGraph.addVertex();
        int bottomMiddleLeft = targetGraph.addVertex();
        int bottomMiddle = targetGraph.addVertex();
        int bottomMiddleRight = targetGraph.addVertex();
        int bottomRight = targetGraph.addVertex();
        targetGraph.addAttribute(topLeft, "label", "topleft");
        targetGraph.addAttribute(topMiddleLeft, "label", "arc");
        targetGraph.addAttribute(topMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(topMiddle, "label", "wire");
        targetGraph.addAttribute(topMiddleRight, "label", "arc");
        targetGraph.addAttribute(topMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(topRight, "label", "topright");
        targetGraph.addAttribute(bottomLeft, "label", "bottomleft");
        targetGraph.addAttribute(bottomMiddleLeft, "label", "arc");
        targetGraph.addAttribute(bottomMiddleLeft, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomMiddle, "label", "wire");
        targetGraph.addAttribute(bottomMiddleRight, "label", "arc");
        targetGraph.addAttribute(bottomMiddleRight, CONFIGURABLE, "1");
        targetGraph.addAttribute(bottomRight, "label", "bottomright");
        targetGraph.addEdge(topLeft, topMiddleLeft);
        targetGraph.addEdge(topMiddleLeft, topMiddle);
        targetGraph.addEdge(topMiddle, topMiddleRight);
        targetGraph.addEdge(topMiddleRight, topRight);
        targetGraph.addEdge(bottomLeft, bottomMiddleLeft);
        targetGraph.addEdge(bottomMiddleLeft, bottomMiddle);
        targetGraph.addEdge(bottomMiddle, bottomMiddleRight);
        targetGraph.addEdge(bottomMiddleRight, bottomRight);
        System.out.println(sourceGraph);
        System.out.println(targetGraph);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(
                new TestCase(sourceGraph, targetGraph, null, null),
                new SettingsBuilder()
                        .withoutContraction()
                        .withKPathRouting().get(),
                10 * 60 * 1000,
                "ConfigurableTest ", false);
        assertTrue(result.succeed);
        System.out.println(result);
        assertArrayEquals(new int[]{0, 1, 4, 5, 6, 9}, ((SuccessResult) result).getVertexPlacement());

        Map<MyEdge, Set<Path>> expected = new HashMap<>();
        expected.put(new MyEdge(0, 1), Util.setOf(new Path(targetGraph, Util.listOf(0, 1))));
        expected.put(new MyEdge(3, 4), Util.setOf(new Path(targetGraph, Util.listOf(5, 6))));
        expected.put(new MyEdge(1, 2), Util.setOf(new Path(targetGraph, Util.listOf(1, 2, 3, 4))));
        expected.put(new MyEdge(4, 5), Util.setOf(new Path(targetGraph, Util.listOf(6, 7, 8, 9))));
        assertEquals(expected.toString(), ((SuccessResult) result).getEdgePlacement().toString());
    }
}
