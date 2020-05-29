package com.charrey.pathiterators.controlpoint;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.PathIterator;

import java.util.HashSet;

public class ManagedControlPointIterator extends PathIterator {


    private final MyGraph graph;
    private final Occupation globalOccupation;
    private final int maxControlPoints;
    private ControlPointIterator child;
    private int controlPoints = 0;

    public ManagedControlPointIterator(MyGraph graph, Vertex tail, Vertex head, Occupation globalOccupation, int maxControlPoints) {
        super(graph.vertexSet().size(), tail, head);
        child = new ControlPointIterator(graph, tail, head, globalOccupation, new HashSet<>(), controlPoints);
        this.graph = graph;
        this.globalOccupation = globalOccupation;
        this.maxControlPoints = maxControlPoints;

    }

    @Override
    public void reset() {
        controlPoints = 0;
        child = new ControlPointIterator(graph, tail(), head(), globalOccupation, new HashSet<>(), controlPoints);
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
            child = new ControlPointIterator(graph, tail(), head(), globalOccupation, new HashSet<>(), controlPoints);
            return next();
        }
    }

    public int getControlPointCount() {
        return controlPoints;
    }

    @Override
    public Object getState() {
        return new Object[]{maxControlPoints, controlPoints, child==null ? null : child.getState()};
    }
}
