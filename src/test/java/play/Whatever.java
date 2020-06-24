package play;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.settings.Settings;

import java.util.Random;

public class Whatever {

    public static void main(String[] args) {
        MyGraph source = new MyGraph(false);
        source.addVertex(0);
        source.addVertex(1);
        source.addVertex(2);
        source.addEdge(0, 1);
        source.addEdge(1, 2);
        source.addEdge(0, 2);

        MyGraph target = new MyGraph(false);
        target.addVertex(0);
        target.addVertex(1);
        target.addVertex(2);
        target.addVertex(3);
        target.addVertex(4);
        target.addEdge(1, 0);
        target.addEdge(0, 2);
        target.addEdge(2, 1);
        target.addEdge(3, 1);
        target.addEdge(2, 3);

        Settings settings = new Settings(true, true, true, 2, 2, new Random(3));

        HomeomorphismResult res = new IsoFinder().getHomeomorphism(new TestCase(source, target), settings, 10_000_000);
        System.out.println(res);
    }
}
