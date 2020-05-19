package com.charrey.router;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.util.datastructures.FakeGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

import java.util.BitSet;
import java.util.Iterator;

public class ControlPointIterator extends PathIterator {

    private final int controlPoints;
    private final BitSet localOccupation;
    private final Vertex head;
    private final Graph<Vertex, DefaultEdge> targetGraph;
    private final Occupation globalOccupation;
    ShortestPathAlgorithm<Vertex, DefaultEdge> spa;

    boolean done = false;
    private final Iterator<Vertex> controlPointCandidates;

    private ControlPointIterator child = null;
    private Vertex chosenControlPoint = null;
    private Path chosenPath = null;

    public ControlPointIterator(Graph<Vertex, DefaultEdge> targetGraph,
                                Vertex tail,
                                Vertex head,
                                Occupation globalOccupation,
                                BitSet localOccupation,
                                int controlPoints) {
        super(targetGraph.vertexSet().size(), tail, head);
        this.targetGraph = targetGraph;
        this.spa = filteredShortestPath(tail, head);
        this.controlPoints = controlPoints;
        this.localOccupation = localOccupation;
        this.globalOccupation = globalOccupation;
        this.head = head;
        this.controlPointCandidates = new ControlPointVertexSelector(targetGraph, globalOccupation, localOccupation, tail, head, spa);
    }

    private ShortestPathAlgorithm<Vertex, DefaultEdge> filteredShortestPath(Vertex tail, Vertex head) {
        return new BidirectionalDijkstraShortestPath<>(new FakeGraph(targetGraph, vertex ->
                vertex != tail && vertex != head &&
                                (globalOccupation.isOccupied(vertex) ||
                                localOccupation.get(vertex.data()) ||
                                Graphs.neighborSetOf(targetGraph, vertex).stream().anyMatch(x -> x != head && x != tail && localOccupation.get(x.data())))));
    }


    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path next() {
        if (controlPoints == 0 && !done) {
            done = true;
            GraphPath<Vertex, DefaultEdge> shortestPath = spa.getPath(tail(), head);
            return shortestPath == null ? null : new Path(shortestPath);
        } else if (done) {
           return null;
        } else if (child == null) {
            chosenControlPoint = controlPointCandidates.next();
            if (chosenControlPoint == null) {
                done = true;
                return null;
            }
            chosenPath = new Path(spa.getPath(chosenControlPoint, head));
            chosenPath.forEach(x -> localOccupation.set(x.data()));
            child = new ControlPointIterator(targetGraph, tail(), chosenControlPoint, globalOccupation, localOccupation, controlPoints - 1);
            return next();
        } else {
            Path childsPath = child.next();
            if (childsPath != null) {
                return merge(childsPath, chosenPath);
            } else {
                child = null;
                chosenPath.forEach(x -> localOccupation.clear(x.data()));
                return next();
            }
        }
    }

    private Path merge(Path childsPath, Path myPath) {
        for (int i = 1; i < myPath.length(); i++) {
            childsPath.append(myPath.get(i));
        }
        return childsPath;
    }

    @Override
    public String toString() {
        return "(" + chosenControlPoint + ", " + child + ")";
    }
}
