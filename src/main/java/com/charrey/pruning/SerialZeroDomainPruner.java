package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

import java.util.Iterator;

public class SerialZeroDomainPruner extends DefaultSerialPruner {

    public SerialZeroDomainPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        super(settings, sourceGraph, targetGraph, occupation);
    }

    @Override
    public Pruner copy() {
        return new SerialZeroDomainPruner(settings, sourceGraph, targetGraph, occupation);
    }

    @Override
    public void close() {

    }

    @Override
    public void checkPartial(PartialMatching partialMatching) throws DomainCheckerException {
        for (int i = 0; i < this.sourceGraph.vertexSet().size(); i++) {
            int finalI = i;
            Iterator<Integer> customIterator = targetGraph.vertexSet().stream().filter(x ->
                    settings.filtering.filter(sourceGraph, targetGraph, finalI, x, occupation)).iterator();
            if (!customIterator.hasNext()) {
                throw new DomainCheckerException("Vertex exists with empty domain: " + i);
            }
        }
    }
}
