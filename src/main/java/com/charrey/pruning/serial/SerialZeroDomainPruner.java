package com.charrey.pruning.serial;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.ReadOnlyOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.pruning.cached.MReachCachedZeroDomainPruner;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.settings.pruning.domainfilter.MReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.NReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntProcedure;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class SerialZeroDomainPruner extends DefaultSerialPruner {


    private final VertexMatching vertexMatching;

    public SerialZeroDomainPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, ReadOnlyOccupation occupation, VertexMatching vertexMatching) {
        super(settings, sourceGraph, targetGraph, occupation);
        this.vertexMatching = vertexMatching;
    }

    @Override
    public Pruner copy() {
        return new SerialZeroDomainPruner(settings, sourceGraph, targetGraph, occupation, vertexMatching);
    }

    @Override
    public void close() {
        //nothing to close
    }

    @Override
    public void checkPartial(PartialMatchingProvider partialMatchingProvider) throws DomainCheckerException {
        if (settings.getFiltering() instanceof LabelDegreeFiltering) {
            for (int i = 0; i < this.sourceGraph.vertexSet().size(); i++) {
                int finalI = i;
                Iterator<Integer> customIterator = targetGraph.vertexSet().stream().filter(x -> new LabelDegreeFiltering().filter(sourceGraph, targetGraph, finalI, x, occupation)).iterator();
                if (!customIterator.hasNext()) {
                    int finalI1 = i;
                    throw new DomainCheckerException(() -> "Vertex exists with empty domain: " + finalI1);
                }
            }
        } else if (settings.getFiltering() instanceof UnmatchedDegreesFiltering) {
            for (int i = 0; i < this.sourceGraph.vertexSet().size(); i++) {
                int finalI = i;
                Iterator<Integer> customIterator = targetGraph.vertexSet().stream().filter(x -> new UnmatchedDegreesFiltering().filter(sourceGraph, targetGraph, finalI, x, occupation)).iterator();
                if (!customIterator.hasNext()) {
                    int finalI1 = i;
                    throw new DomainCheckerException(() -> "Vertex exists with empty domain: " + finalI1);
                }
            }
        } else if (settings.getFiltering() instanceof MReachabilityFiltering) {
            Pruner innerPruner = new MReachCachedZeroDomainPruner(settings, sourceGraph, targetGraph, occupation, vertexMatching, 0);
            PartialMatching partialMatching = partialMatchingProvider.getPartialMatching();
            int[] vertexMapping = partialMatching.getVertexMapping().toArray();

            throw new UnsupportedOperationException(); //todo;
        } else if (settings.getFiltering() instanceof NReachabilityFiltering) {
            new GlobalOccupation(); //todo new occupation, to simulate that nothing has happened yet
            Pruner innerPruner = new MReachCachedZeroDomainPruner(settings, sourceGraph, targetGraph, occupation, vertexMatching, ((NReachabilityFiltering)settings.getFiltering()).getLevel());
            PartialMatching partialMatching = partialMatchingProvider.getPartialMatching();
            int[] vertexMapping = partialMatching.getVertexMapping().toArray();
            TIntObjectMap<Set<Path>> edgeMapping = partialMatching.getEdgeMapping();
            for (int i = 0; i < vertexMapping.length; i++) {
                innerPruner.beforeOccupyVertex(i + 1, vertexMapping[i], null);
                Set<Path> paths = edgeMapping.get(i);
                for (Path path : paths) {
                    Path intermediate = path.intermediate();
                    for (Integer routingVertex : intermediate) {
                        innerPruner.afterOccupyEdge(vertexMapping.length, routingVertex, null);
                    }
                }
            }
        }
    }
}
