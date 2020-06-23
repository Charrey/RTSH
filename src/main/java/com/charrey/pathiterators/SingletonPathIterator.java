package com.charrey.pathiterators;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingletonPathIterator extends PathIterator {

    private boolean done = false;
    @NotNull
    private final Path toReturn;

    SingletonPathIterator(@NotNull MyGraph graph, @NotNull Vertex tail, @NotNull Vertex head) {
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
