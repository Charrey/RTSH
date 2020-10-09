package com.charrey.algorithms.vertexordering;

import com.charrey.graph.MyGraph;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomOrder implements GraphVertexMapper {

    private final Random random;

    public RandomOrder(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public Mapping apply(@NotNull MyGraph graph) {
        List<Integer> newToOldList = new ArrayList<>(graph.vertexSet());
        Collections.shuffle(newToOldList, random);
        int[] newToOld = newToOldList.stream().mapToInt(x -> x).toArray();
        int[] oldToNew = new int[newToOld.length];
        for (int i = 0; i < newToOld.length; i++) {
            oldToNew[newToOld[i]] = i;
        }
        MyGraph toReturn = MyGraph.applyOrdering(graph, newToOld, oldToNew);
        return new Mapping(toReturn, newToOld);
    }
}
