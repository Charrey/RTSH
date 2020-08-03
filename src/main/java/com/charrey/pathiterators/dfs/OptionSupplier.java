package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;

public abstract class OptionSupplier {

    private final int head;
    private final MyGraph graph;

    OptionSupplier(MyGraph graph, int head) {
        this.graph = graph;
        this.head = head;
    }
    public abstract int get(int at, int option, Path currentPath);

    int getHead() {
        return head;
    }

    MyGraph getGraph() {
        return graph;
    }
}
