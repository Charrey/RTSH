package com.charrey.pathiterators;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingletonPathIterator extends PathIterator {

    private boolean done = false;
    @NotNull
    private final Path toReturn;

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
