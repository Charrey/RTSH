package com.charrey.matching.candidate;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

import java.util.*;

public class RandomIterator extends VertexCandidateIterator {

    private final int[] vertices;
    int indexToReturn = 0;


    public RandomIterator(MyGraph sourceGraph, MyGraph targetGraph, Settings settings, GlobalOccupation occupation, int sourceGraphVertex) {
        super(sourceGraph, targetGraph, settings, occupation, sourceGraphVertex);
        List<Integer> vertexList = new ArrayList<>();
        Iterator<Integer> innerIterator = getInnerIterator();
        while (innerIterator.hasNext()) {
            vertexList.add(innerIterator.next());
        }
        Collections.shuffle(vertexList, new Random(settings.nextLong()));
        vertices = vertexList.stream().mapToInt(x -> x).toArray();
    }

    @Override
    public void doReset() {
        this.nextToReturn = ABSENT;
        indexToReturn = 0;
    }

    @Override
    protected void prepareNextToReturn() {
        if (indexToReturn >= vertices.length) {
            nextToReturn = EXHAUSTED;
        } else {
            nextToReturn = vertices[indexToReturn];
            indexToReturn++;
        }
    }
}
