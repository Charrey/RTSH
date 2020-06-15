package com.charrey.pathiterators;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingletonPathIterator extends PathIterator {

    private boolean done = false;
    @NotNull
    private final Path toReturn;

    SingletonPathIterator(@NotNull Vertex tail, @NotNull Vertex head) {
        super(tail, head, true);
        toReturn = new Path(tail, 2);
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

}
