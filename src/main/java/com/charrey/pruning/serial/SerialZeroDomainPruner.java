package com.charrey.pruning.serial;

import com.charrey.algorithms.UtilityData;
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
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.settings.pruning.domainfilter.MReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.NReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntProcedure;
import org.jgrapht.Graphs;

import java.util.*;

public class SerialZeroDomainPruner extends DefaultSerialPruner {


    private final VertexMatching vertexMatching;

    public SerialZeroDomainPruner(Settings settings,
                                  MyGraph sourceGraph,
                                  MyGraph targetGraph,
                                  ReadOnlyOccupation occupation,
                                  VertexMatching vertexMatching) {
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

    private boolean checkedOnce = false;

    @Override
    public void checkPartial(PartialMatchingProvider partialMatchingProvider, int vertexPlaced) throws DomainCheckerException {
        PartialMatching partialMatching = partialMatchingProvider.getPartialMatching();
        if (settings.getFiltering() instanceof LabelDegreeFiltering) {
            if (!checkedOnce) {
                checkedOnce = true;
                for (int i = partialMatching.getVertexMapping().size(); i < this.sourceGraph.vertexSet().size(); i++) {
                    int finalI = i;
                    Iterator<Integer> customIterator = targetGraph.vertexSet()
                            .stream()
                            .filter(x -> new LabelDegreeFiltering().filter(sourceGraph, targetGraph, finalI, x, occupation))
                            .filter(x -> !occupation.isOccupied(x))
                            .iterator();
                    if (!customIterator.hasNext()) {
                        int finalI1 = i;
                        throw new DomainCheckerException(() -> "Vertex exists with empty domain: " + finalI1);
                    }
                }
            }
        } else if (settings.getFiltering() instanceof UnmatchedDegreesFiltering) {
            //get previous partial matching
            for (int i = partialMatching.getVertexMapping().size(); i < this.sourceGraph.vertexSet().size(); i++) {
                int finalI = i;
                Iterator<Integer> customIterator = (vertexPlaced > -1 ? Graphs.neighborListOf(targetGraph, vertexPlaced) : targetGraph.vertexSet())
                        .stream()
                        .filter(x -> new UnmatchedDegreesFiltering().filter(sourceGraph, targetGraph, finalI, x, occupation))
                        .filter(x -> !occupation.isOccupied(x))
                        .iterator();
                if (!customIterator.hasNext()) {
                    int finalI1 = i;
                    throw new DomainCheckerException(() -> "Vertex exists with empty domain: " + finalI1);
                }
            }
        } else if (settings.getFiltering() instanceof MReachabilityFiltering || settings.getFiltering() instanceof NReachabilityFiltering) {
            GlobalOccupation occ = new GlobalOccupation(new UtilityData(sourceGraph, targetGraph), new SettingsBuilder(settings).withCachedPruning().get());
            occ.init(vertexMatching);
            int[] vertexMapping = partialMatching.getVertexMapping().toArray();
            TIntObjectMap<Set<Path>> edgeMapping = partialMatching.getEdgeMapping();
            for (int i = 0; i < vertexMapping.length; i++) {
                occ.occupyVertex(i + 1, vertexMapping[i], partialMatching);
                if (i + 1 < edgeMapping.size()) {
                    List<Path> paths = new ArrayList<>(edgeMapping.get(i+1));
                    Collections.sort(paths);
                    for (Path path : paths) {
                        for (Integer intermediateVertex : path.intermediate()) {
                            occ.occupyRoutingAndCheck(i + 1, intermediateVertex, null);
                        }
                    }
                }
            }
            TIntList sortedList = new TIntArrayList(partialMatching.getPartialPath());
            sortedList.sort();
            if (!sortedList.forEach(value -> {
                try {
                    occ.occupyRoutingAndCheck(vertexMapping.length - 1, value, null);
                    return true;
                } catch (DomainCheckerException e) {
                    return false;
                }
            })) {
                throw new DomainCheckerException(() -> "Partial path was not okay");
            }
        }
    }
}
