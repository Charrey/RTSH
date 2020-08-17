package com.charrey.pathiterators.kpath;

import com.charrey.graph.MyEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.GraphWalk;

import java.util.*;

public class LoopShortestPathIterator implements
        Iterator<GraphPath<Integer, MyEdge>> {


    private final int sourceSink;
    double[] distances;
    int[] neighbours;
    List<YenShortestPathIterator<Integer, MyEdge>> spas;
    PriorityQueue<Pair<Integer, GraphPath<Integer, MyEdge>>> pathQueue;

    public LoopShortestPathIterator(Graph<Integer, MyEdge> graph, int sourceSink) {
        this.sourceSink = sourceSink;
        neighbours = Graphs.successorListOf(graph, sourceSink).stream().filter(x -> x != sourceSink).mapToInt(x -> x).toArray();
        distances = new double[neighbours.length];
        spas = new ArrayList<>(neighbours.length);
        pathQueue = new PriorityQueue<>(neighbours.length, Comparator.comparingDouble(o -> distances[o.getFirst()] + o.getSecond().getWeight()));
        for (int i = 0; i < neighbours.length; i++) {
            distances[i] = graph.getEdgeWeight(graph.getEdge(sourceSink, neighbours[i]));
            spas.add(new YenShortestPathIterator<>(graph, neighbours[i], sourceSink));
            GraphPath<Integer, MyEdge> path = spas.get(i).next();
            if (path != null) {
                pathQueue.add(new Pair<>(i, path));
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !pathQueue.isEmpty();
    }

    @Override
    public GraphPath<Integer, MyEdge> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        } else {
            Pair<Integer, GraphPath<Integer, MyEdge>> next = pathQueue.poll();
            if (spas.get(next.getFirst()).hasNext()) {
                GraphPath<Integer, MyEdge> nextPath = spas.get(next.getFirst()).next();
                pathQueue.add(new Pair<>(next.getFirst(), nextPath));
            }
            GraphPath<Integer, MyEdge> path = next.getSecond();
            List<Integer> vertexList = path.getVertexList();
            vertexList.add(0, sourceSink);
            return new GraphWalk<>(path.getGraph(), vertexList, path.getWeight() + distances[next.getFirst()]);
        }
    }
}
