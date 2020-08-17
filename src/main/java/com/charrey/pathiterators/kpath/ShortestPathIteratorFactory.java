package com.charrey.pathiterators.kpath;

import com.charrey.graph.MyEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;

import java.util.Iterator;

public class ShortestPathIteratorFactory {

    public static Iterator<GraphPath<Integer, MyEdge>> get(Graph<Integer, MyEdge> graph, int source, int sink) {
        if (source == sink) {
            return new LoopShortestPathIterator(graph, source);
        } else {
            return new YenShortestPathIterator<>(graph, source, sink);
        }
    }
}
