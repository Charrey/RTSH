package com.charrey.router;

import com.charrey.Occupation;
import com.charrey.graph.Vertex;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class ControlPointVertexSelector implements Iterator<Vertex> {

    private final Occupation occupation;
    private final Graph<Vertex, DefaultEdge> graph;
    private final Iterator<Vertex> graphIndexIterator;
    private final List<Vertex> vertices;
    private final BitSet local;
    private final Vertex to;
    private final ShortestPathAlgorithm<Vertex, DefaultEdge> spa;
    private final Vertex connector;
    private final Vertex from;
    int indexTried = -1;

    private Set<Vertex> outlawed = new HashSet<>();



    public ControlPointVertexSelector(Graph<Vertex, DefaultEdge> graph,
                                      Occupation occupation,
                                      BitSet local,
                                      Vertex from,
                                      Vertex to,
                                      ShortestPathAlgorithm<Vertex, DefaultEdge> spa) {
        this.graph = graph;
        this.occupation = occupation;
        this.local = local;
        this.from = from;
        this.to = to;
        graphIndexIterator = graph.vertexSet().iterator();
        vertices = GraphUtil.randomVertexOrder(graph);
        GraphPath<Vertex, DefaultEdge> path = new BidirectionalDijkstraShortestPath<>(graph).getPath(from, to);
        if (path == null) {
            readyToDeliver = true;
            nextToReturn = null;
            this.spa = null;
            this.connector = null;
        } else {
            List<Vertex> shortestPathFromSource = path.getVertexList();
            outlawed.addAll(shortestPathFromSource);
            this.connector = shortestPathFromSource.get(shortestPathFromSource.size() - 2);
            this.spa = spa;
        }
    }

    Vertex nextToReturn = null;
    boolean readyToDeliver = false;


    @Override
    public boolean hasNext() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
            readyToDeliver = true;
        }
        return nextToReturn != null;
    }

    private Vertex iterate() {
        while (true) {
            int newIndex = indexTried + 1;
            if (newIndex >= vertices.size()) {
                return null;
            }
            Vertex vertex = vertices.get(newIndex);
            indexTried = newIndex;
            if (isSuitable(vertex)) {
                return vertex;
            }
            indexTried = newIndex;
        }
    }

    private boolean isSuitable(Vertex vertex) {
        if (occupation.isOccupied(vertex)) {
            return false;
        } else if (outlawed.contains(vertex)) {
            return false;
        } else if (GraphUtil.neighboursOf(graph, vertex).stream().anyMatch(v -> local.get(v.data()) && v != to)) {
            return false;
        } else if (!hasImpact(vertex)) {
            return false;
        } else return !betterCandidateExists(vertex);
    }

    private boolean betterCandidateExists(Vertex vertex) {
        List<Vertex> path = spa.getPath(vertex, to).getVertexList();
        for (int i = 1; i < path.size() - 1; i++) {
            if (this.isSuitable(path.get(i))) {
                GraphPath<Vertex, DefaultEdge> pathToThat = spa.getPath(from, path.get(i));
                if (pathToThat != null && pathToThat.getVertexList().contains(vertex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasImpact(Vertex vertex) {
        GraphPath<Vertex, DefaultEdge> path;
        try {
            path = spa.getPath(vertex, to);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (path == null) {
            return false;
        }
        List<Vertex> pathFromHere = path.getVertexList();
        return pathFromHere.get(pathFromHere.size() - 2) != connector;
        //if the shortest path to this vertex has a different connection to the target vertex than the shortest path has, ignore
    }

    @Override
    public Vertex next() {
        if (!readyToDeliver) {
            nextToReturn = iterate();
        }
        readyToDeliver = false;
        return nextToReturn;
    }


}
