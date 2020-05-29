package com.charrey.pathiterators.controlpoint;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.PathIterator;
import com.charrey.settings.Settings;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ControlPointIterator extends PathIterator {

    private final int controlPoints;
    private final Set<Integer> localOccupation;
    private final Vertex head;
    private final MyGraph targetGraph;
    private final Occupation globalOccupation;

    boolean done = false;
    private final Iterator<Vertex> controlPointCandidates;

    private ControlPointIterator child = null;
    private Vertex chosenControlPoint = null;
    private Path chosenPath = null;

    public ControlPointIterator(MyGraph targetGraph,
                                Vertex tail,
                                Vertex head,
                                Occupation globalOccupation,
                                Set<Integer> localOccupation,
                                int controlPoints) {
        super(targetGraph.vertexSet().size(), tail, head);
        this.targetGraph = targetGraph;
        this.controlPoints = controlPoints;
        this.localOccupation = localOccupation;
        this.globalOccupation = globalOccupation;
        this.head = head;
        this.controlPointCandidates = new ControlPointVertexSelector(targetGraph, globalOccupation, localOccupation, tail, head);
    }

    public static GraphPath<Vertex, DefaultEdge> filteredShortestPath(MyGraph targetGraph, Occupation globalOccupation, Set<Integer> localOccupation, Vertex tail, Vertex head) {
        Graph<Vertex, DefaultEdge> fakeGraph = new MaskSubgraph<>(targetGraph, vertex ->
                vertex != tail && vertex != head &&
                        (globalOccupation.isOccupied(vertex) ||
                                localOccupation.contains(vertex.data()) ||
                                (Settings.instance.refuseLongerPaths &&
                                Graphs.neighborSetOf(targetGraph, vertex).stream().anyMatch(x -> x != head && x != tail && localOccupation.contains(x.data())))),
                y -> false);
        return new BidirectionalDijkstraShortestPath<>(fakeGraph).getPath(tail, head);
    }

    private GraphPath<Vertex, DefaultEdge> filteredShortestPath(Vertex from, Vertex to) {
        return ControlPointIterator.filteredShortestPath(targetGraph, globalOccupation, localOccupation, from, to);
    }


    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path next() {
        if (controlPoints == 0 && !done) {
            done = true;
            GraphPath<Vertex, DefaultEdge> shortestPath = filteredShortestPath(tail(), head);
            assert shortestPath == null || new Path(shortestPath).intermediate().stream().noneMatch(x -> localOccupation.contains(x.data()));
            return shortestPath == null ? null : new Path(shortestPath);
        } else if (done) {
           return null;
        } else if (child == null) {
            chosenControlPoint = controlPointCandidates.next();
            if (chosenControlPoint == null) {
                done = true;
                return null;
            }
            GraphPath<Vertex, DefaultEdge> graphPath = filteredShortestPath(chosenControlPoint, head);
            if (graphPath != null) {
                chosenPath = new Path(graphPath);
                chosenPath.forEach(x -> localOccupation.add(x.data()));
                child = new ControlPointIterator(targetGraph, tail(), chosenControlPoint, globalOccupation, new HashSet<>(localOccupation), controlPoints - 1);
            }
            return next();
        } else {
            Set<Integer> previousLocalOccupation = new HashSet<>(localOccupation);
            Path childsPath = child.next();
            assert childsPath == null || childsPath.intermediate().stream().noneMatch(x -> previousLocalOccupation.contains(x.data()));
            if (childsPath != null) {
                assert childsPath.tail() == tail();
                assert chosenPath.head() == head;
                Path toReturn =  merge(childsPath, chosenPath);
                assert toReturn.tail() == tail();
                assert toReturn.head() == head;
                return toReturn;
            } else {
                child = null;
                chosenPath.forEach(x -> localOccupation.remove(x.data()));
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

    @Override
    public Object getState() {
        return new Object[]{head(), head, tail(), controlPoints, chosenControlPoint, child == null ? null : child.getState()};
    }
}
