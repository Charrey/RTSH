package contraction;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
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
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 10*60*1000, "ContractionTest", false);
        assert result instanceof SuccessResult;
    }

    @Test
    public void withoutLabel() {
        MyGraph sourceGraph = new MyGraph(true);
        int s1 = sourceGraph.addVertex();
        int s2 = sourceGraph.addVertex();
        int s3 = sourceGraph.addVertex();
        sourceGraph.addEdge(s1, s2);
        sourceGraph.addEdge(s2, s3);

        MyGraph targetGraph = new MyGraph(true);
        int t1 = targetGraph.addVertex();
        int t2 = targetGraph.addVertex();
        int t3 = targetGraph.addVertex();
        int t4 = targetGraph.addVertex();
        int t5 = targetGraph.addVertex();

        targetGraph.addEdge(t1, t2);
        targetGraph.addEdge(t3, t4);
        targetGraph.addEdge(t4, t5);

        Settings settings = new SettingsBuilder().withContraction().get();
        TestCase testCase = new TestCase(sourceGraph, targetGraph, null, null);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 10*60*1000, "ContractionTest", false);
        System.out.println(result);
        assert result instanceof SuccessResult;
    }



    @Test
    public void retryContractedTest() {
        MyGraph sourceGraph = new MyGraph(true);
        int sa = sourceGraph.addVertex();
        int sb = sourceGraph.addVertex();
        int sc = sourceGraph.addVertex();
        int sd = sourceGraph.addVertex();
        int se = sourceGraph.addVertex();
        int sf = sourceGraph.addVertex();
        int sg = sourceGraph.addVertex();
        int sh = sourceGraph.addVertex();
        int si = sourceGraph.addVertex();
        sourceGraph.addAttribute(se, "label", "muffin");
        sourceGraph.addEdge(sa, sb);
        sourceGraph.addEdge(sa, sc);
        sourceGraph.addEdge(sa, sd);
        sourceGraph.addEdge(sa, se);
        sourceGraph.addEdge(se, sf);
        sourceGraph.addEdge(sf, sg);
        sourceGraph.addEdge(sf, sh);
        sourceGraph.addEdge(sh, si);

        MyGraph targetGraph = new MyGraph(true);
        int ta = targetGraph.addVertex();
        int tb = targetGraph.addVertex();
        int tc = targetGraph.addVertex();
        int td = targetGraph.addVertex();
        int te = targetGraph.addVertex();
        int tf = targetGraph.addVertex();
        int tg = targetGraph.addVertex();
        int th = targetGraph.addVertex();
        int ti = targetGraph.addVertex();
        int tj = targetGraph.addVertex();
        int tk = targetGraph.addVertex();
        int tl = targetGraph.addVertex();
        int tm = targetGraph.addVertex();
        int tn = targetGraph.addVertex();
        targetGraph.addAttribute(te, "label", "muffin");
        targetGraph.addAttribute(tj, "label", "muffin");
        targetGraph.addEdge(ta, tb);
        targetGraph.addEdge(ta, tc);
        targetGraph.addEdge(ta, td);
        targetGraph.addEdge(ta, te);
        targetGraph.addEdge(ta, tj);
        targetGraph.addEdge(te, tf);
        targetGraph.addEdge(tf, tg);
        targetGraph.addEdge(tf, ti);
        targetGraph.addEdge(tf, th);
        targetGraph.addEdge(tj, tk);
        targetGraph.addEdge(tk, tn);
        targetGraph.addEdge(tk, tl);
        targetGraph.addEdge(tl, tm);

        Settings settings = new SettingsBuilder().withContraction().get();
        TestCase testCase = new TestCase(sourceGraph, targetGraph, null, null);
        HomeomorphismResult result = new IsoFinder().getHomeomorphism(testCase, settings, 10*60*1000, "ContractionTest", false);
        System.out.println(result);
        assert result instanceof SuccessResult;
    }

}