package jgrapht;

import com.charrey.graph.Vertex;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;
import org.jgrapht.graph.DefaultEdge;

public class DoesJgraphtCacheShortestPaths {

    public static void main(String[] args) {

        for (int v = 5000; v <= 5000; v*=1.5) {
            long e = (long) (v * 1.8);
            RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(v, (int) e, 0, 0, 1920);
            gen.init(1, false);
            MyGraph graph = gen.getNext().sourceGraph;
            long start = System.nanoTime();
            ManyToManyShortestPathsAlgorithm<Integer, DefaultEdge> spa = new CHManyToManyShortestPaths<>(graph);
            spa.getManyToManyPaths(graph.vertexSet(), graph.vertexSet());
            long end = System.nanoTime();

            System.out.println(v + "\t" + e + "\t" + (end - start));

        }
    }

}
