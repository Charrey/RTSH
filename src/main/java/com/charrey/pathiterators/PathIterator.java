package com.charrey.pathiterators;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.pathiterators.dfs.DFSPathIterator;
import com.charrey.pathiterators.yen.YenPathIterator;
import com.charrey.settings.Settings;
import com.charrey.util.UtilityData;

import java.util.function.Supplier;

import static com.charrey.settings.PathIterationStrategy.*;

public abstract class PathIterator {


    private final Vertex head;
    private final Vertex tail;

    protected PathIterator(Vertex tail, Vertex head) {
        this.tail = tail;
        this.head = head;
    }

    public static PathIterator get(MyGraph targetGraph, UtilityData data, Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> placementSize) {
        if (targetGraph.getEdge(tail, head) != null) {
            return new SingletonPathIterator(tail, head);
        }

        switch (Settings.instance.pathIteration) {
            case DFS_ARBITRARY:
            case DFS_GREEDY:
                Vertex[][] targetNeighbours = data.getTargetNeighbours(Settings.instance.pathIteration)[head.data()];
                return new DFSPathIterator(targetNeighbours, tail, head, occupation, placementSize);
            case CONTROL_POINT:
                return new ManagedControlPointIterator(targetGraph, tail, head, occupation, 300);
            case YEN:
                return new YenPathIterator(targetGraph, tail, head, occupation, placementSize);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public abstract Path next();

    public Vertex tail() {
        return tail;
    }

    public Vertex head() {
        return head;
    }

}
