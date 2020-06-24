package com.charrey.pathiterators;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A pathiterator for paths with no intermediate vertices. This iterates exactly once, in which it returns the path
 * directly from the source to the target.
 */
public class SingletonPathIterator extends PathIterator {

    private boolean done = false;
    @NotNull
    private final Path toReturn;

    /**
     * Instantiates a new SingletonPathIterator.
     *
     * @param graph the target graph
     * @param tail  the source vertex of the path
     * @param head  the target vertex of the path
     * @throws IllegalStateException thrown if the tail vertex has no outgoing edge to the head.
     */
    SingletonPathIterator(@NotNull MyGraph graph, int tail, int head) {
        super(tail, head, true);
        toReturn = new Path(graph, tail);
        toReturn.append(head);
    }

    @Nullable
    @Override
    public Path next() {
        if (done) {
            return null;
        } else {
            done = true;
            return toReturn;
        }
    }

    @Override
    public String debugInfo() {
        return "single";
    }

}
