package com.charrey.pathiterators.dfs;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import org.jgrapht.Graphs;

import java.util.Collections;
import java.util.List;

public class IndexOptionSupplier extends OptionSupplier {

    IndexOptionSupplier(MyGraph graph, int head) {
        super(graph, head);
    }

    @Override
    public int get(int at, int option, Path currentPath) {
        assert at != getHead();
        List<Integer> successors = Graphs.successorListOf(getGraph(), at);
        Collections.sort(successors);
        if (option >= successors.size()) {
            return -1;
        } else {
            //System.out.println("Option " + option + " from " + at + " to " + getHead() + " is: " + successors.get(option));
            return successors.get(option);
        }
    }
}
