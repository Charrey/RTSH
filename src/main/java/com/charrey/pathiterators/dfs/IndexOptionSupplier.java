package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import org.jgrapht.Graphs;

import java.util.List;

public class IndexOptionSupplier extends OptionSupplier {

    IndexOptionSupplier(MyGraph graph, int head) {
        super(graph, head);
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        assert at != getHead();
        List<Integer> successors = Graphs.successorListOf(getGraph(), at);
        if (option >= successors.size()) {
            return -1;
        } else {
            return successors.get(option);
        }
    }
}
