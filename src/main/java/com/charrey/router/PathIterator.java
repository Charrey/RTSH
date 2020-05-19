package com.charrey.router;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.util.Settings;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.Indexable;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.function.Supplier;

public abstract class PathIterator implements Indexable {


    private final Vertex head;
    private final Vertex tail;
    private final int targetGraphSize;

    public PathIterator(int targetGraphSize, Vertex tail, Vertex head) {
        this.targetGraphSize = targetGraphSize;
        this.tail = tail;
        this.head = head;
    }

    public static PathIterator get(Graph<Vertex, DefaultEdge> targetGraph, UtilityData data, Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> placementSize) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(targetGraph.vertexSet().size(), tail, head);
        }

        switch (Settings.pathIteration) {
            case DFS_ARBITRARY:
            case DFS_GREEDY:
                Vertex[][] targetNeighbours = data.getTargetNeighbours(Settings.pathIteration)[head.data()];
                return new DFSPathIterator(targetGraph.vertexSet().size(), targetNeighbours, tail, head, occupation, placementSize);
            case CONTROL_POINT:
                return new ManagedControlPointIterator(targetGraph, tail, head, occupation, 4);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract void reset();

    public abstract Path next();

    public Vertex tail() {
        return tail;
    }

    public Vertex head() {
        return head;
    }

    public int data() {
        return (targetGraphSize + 1) * head.data() + tail.data();
    }
}
