package com.charrey.pruning.serial;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.ReadOnlyOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.settings.pruning.domainfilter.MReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.NReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jgrapht.Graphs;

import java.util.*;

public class SerialAllDifferentPruner extends DefaultSerialPruner {

    private final VertexMatching vertexMatching;
    private AllDifferent allDifferent = new AllDifferent();

    public SerialAllDifferentPruner(Settings settings,
                                    MyGraph sourceGraph,
                                    MyGraph targetGraph,
                                    ReadOnlyOccupation occupation,
                                    VertexMatching vertexMatching) {
        super(settings, sourceGraph, targetGraph, occupation);
        this.vertexMatching = vertexMatching;
    }

    @Override
    public Pruner copy() {
        return new SerialAllDifferentPruner(settings, sourceGraph, targetGraph, occupation, vertexMatching);
    }

    @Override
    public void close() {

    }

    private boolean checkedOnce = false;

    @Override
    public void checkPartial(PartialMatchingProvider partialMatchingProvider, int vertexPlaced) throws DomainCheckerException {
        PartialMatching partialMatching = partialMatchingProvider.getPartialMatching();
        GlobalOccupation occ = new GlobalOccupation(new UtilityData(sourceGraph, targetGraph), new SettingsBuilder(settings).withCachedPruning().get());
        occ.init(vertexMatching);
        List<Integer> vertexMapping = partialMatching.getVertexMapping();
        TIntObjectMap<Set<Path>> edgeMapping = partialMatching.getEdgeMapping();
        for (int i = 0; i < vertexMapping.size(); i++) {
            occ.occupyVertex(i + 1, vertexMapping.get(i), partialMatching);
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
                occ.occupyRoutingAndCheck(vertexMapping.size() - 1, value, null);
                return true;
            } catch (DomainCheckerException e) {
                return false;
            }
        })) {
            throw new DomainCheckerException(() -> "Partial path was not okay");
        }
    }
}
