package com.charrey.router;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.BitSet;

public class ManagedControlPointIterator extends PathIterator {


    private final Graph<Vertex, DefaultEdge> graph;
    private final Occupation globalOccupation;
    private final int maxControlPoints;
    private ControlPointIterator child;
    private int controlPoints = 0;

    public ManagedControlPointIterator(Graph<Vertex, DefaultEdge> graph, Vertex tail, Vertex head, Occupation globalOccupation, int maxControlPoints) {
        super(graph.vertexSet().size(), tail, head);
        child = new ControlPointIterator(graph, tail, head, globalOccupation, new BitSet(graph.vertexSet().size()), controlPoints);
        this.graph = graph;
        this.globalOccupation = globalOccupation;
        this.maxControlPoints = maxControlPoints;

    }

    @Override
    public void reset() {
        controlPoints = 0;
        child = new ControlPointIterator(graph, tail(), head(), globalOccupation, new BitSet(graph.vertexSet().size()), controlPoints);
    }

    @Override
    public Path next() {
        Path path = child.next();
        if (path != null) {
            //System.out.println(child);
            return path;
        } else {
            if (controlPoints + 1 > maxControlPoints) {
                return null;
            }
            controlPoints += 1;
            child = new ControlPointIterator(graph, tail(), head(), globalOccupation, new BitSet(graph.vertexSet().size()), controlPoints);
            return next();
        }
    }

    public int getControlPointCount() {
        return controlPoints;
    }
}
