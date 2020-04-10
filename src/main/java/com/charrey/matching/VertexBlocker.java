package com.charrey.matching;

import com.charrey.graph.Vertex;

import java.util.HashSet;
import java.util.Set;

public abstract class VertexBlocker {

    private Set<VertexBlocker> included = new HashSet<>();

    public void include (VertexBlocker toadd) {
        this.included.add(toadd);
    }

    public final boolean blocks(Vertex v) {
        return blocksNonRecursive(v) || included.stream().anyMatch(x -> x.blocksNonRecursive(v));
    }

    public abstract boolean blocksNonRecursive(Vertex v);
}
