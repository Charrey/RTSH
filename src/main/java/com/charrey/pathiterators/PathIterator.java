package com.charrey.pathiterators;

import com.charrey.Occupation;
import com.charrey.Stateable;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.yen.YenPathIterator;
import com.charrey.settings.Settings;
import com.charrey.util.UtilityData;
import com.charrey.util.datastructures.Indexable;

import java.util.function.Supplier;

import static com.charrey.settings.PathIterationStrategy.*;

public abstract class PathIterator implements Indexable, Stateable {


    private final Vertex head;
    private final Vertex tail;
    private final int targetGraphSize;

    public PathIterator(int targetGraphSize, Vertex tail, Vertex head) {
        this.targetGraphSize = targetGraphSize;
        this.tail = tail;
        this.head = head;
    }

    public static PathIterator get(MyGraph targetGraph, UtilityData data, Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> placementSize) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(targetGraph.vertexSet().size(), tail, head);
        }

        switch (Settings.instance.pathIteration) {
            case DFS_ARBITRARY:
            case DFS_GREEDY:
                Vertex[][] targetNeighbours = data.getTargetNeighbours(Settings.instance.pathIteration)[head.data()];
                return new DFSPathIterator(targetGraph.vertexSet().size(), targetNeighbours, tail, head, occupation, placementSize);
            case CONTROL_POINT:
                return new ManagedControlPointIterator(targetGraph, tail, head, occupation, 3);
            case YEN:
                return new YenPathIterator(targetGraph, tail, head, occupation, placementSize);
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
