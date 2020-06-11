package com.charrey.pathiterators;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;

public class SingletonPathIterator extends PathIterator {

    boolean done = false;
    private final Path toReturn;

    public SingletonPathIterator(Vertex tail, Vertex head) {
        super(tail, head);
        toReturn = new Path(tail, 2);
        toReturn.append(head);
    }

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
