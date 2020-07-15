package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexCandidateIterator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;

public class SerialZeroDomainPruner extends DefaultSerialPruner {

    public SerialZeroDomainPruner(FilteringSettings filter, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        super(filter, sourceGraph, targetGraph, occupation);
    }

    @Override
    public Pruner copy() {
        return new SerialZeroDomainPruner(filter, sourceGraph, targetGraph, occupation);
    }

    @Override
    public void checkPartial(PartialMatching partialMatching) throws DomainCheckerException {
        for (int i = 0; i < this.sourceGraph.vertexSet().size(); i++) {
            if (!new VertexCandidateIterator(sourceGraph, targetGraph, i, filter, occupation).hasNext()) {
                throw new DomainCheckerException("Vertex exists with empty domain");
            }
        }
    }
}
