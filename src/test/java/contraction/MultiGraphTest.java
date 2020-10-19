package contraction;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.Test;

public class MultiGraphTest {

    @Test
    public void testMultipleEdges() {
        MyGraph source = new MyGraph(true);
        int a = source.addVertex();
        int b = source.addVertex();
        source.addEdge(a, b);
        source.addEdge(a, b);
        source.addEdge(a, b);
        MyGraph target = new MyGraph(true);
        a = target.addVertex();
        b = target.addVertex();
        target.addEdge(a, b);
        target.addEdge(a, b);

        int c = target.addVertex();
        target.addEdge(a, c);
        target.addEdge(c, b);
        System.out.println();

        TestCase testCase = new TestCase(source, target, null, null);
        Settings settings = new SettingsBuilder().withKPathRouting().withoutContraction().get();

        HomeomorphismResult result = new IsoFinder(settings).getHomeomorphism(testCase, 100000L, "MultigraphTest", false);
        System.out.println(result);
    }

    @Test
    public void testLoopOnCycle() {
        MyGraph source = new MyGraph(true);
        int a = source.addVertex();
        source.addEdge(a, a);

        MyGraph target = new MyGraph(true);
        a = target.addVertex();
        int b = target.addVertex();
        target.addEdge(a, b);
        target.addEdge(b, a);

        TestCase testCase = new TestCase(source, target, null, null);
        Settings settings = new SettingsBuilder().withControlPointRouting().withoutContraction().get();

        HomeomorphismResult result = new IsoFinder(settings).getHomeomorphism(testCase, 100000L, "MultigraphTest", false);
        System.out.println(result);
    }

    @Test
    public void testLoopOnLoop() {
        MyGraph source = new MyGraph(true);
        int a = source.addVertex();
        source.addEdge(a, a);

        MyGraph target = new MyGraph(true);
        a = target.addVertex();
        target.addEdge(a, a);

        TestCase testCase = new TestCase(source, target, null, null);
        Settings settings = new SettingsBuilder().withControlPointRouting().withoutContraction().get();

        HomeomorphismResult result = new IsoFinder(settings).getHomeomorphism(testCase, 100000L, "MultigraphTest", false);
        System.out.println(result);
    }

    @Test
    public void testMultipleLoops() {
        MyGraph source = new MyGraph(true);
        int a = source.addVertex();
        source.addEdge(a, a);
        source.addEdge(a, a);
        source.addEdge(a, a);

        MyGraph target = new MyGraph(true);
        a = target.addVertex();
        target.addEdge(a, a);
        target.addEdge(a, a);
        int b = target.addVertex();
        target.addEdge(a, b);
        target.addEdge(b, a);

        TestCase testCase = new TestCase(source, target, null, null);
        Settings settings = new SettingsBuilder().withControlPointRouting().withoutContraction().get();

        HomeomorphismResult result = new IsoFinder(settings).getHomeomorphism(testCase, 100000L, "MultigraphTest", false);
        System.out.println(result);
    }
}
