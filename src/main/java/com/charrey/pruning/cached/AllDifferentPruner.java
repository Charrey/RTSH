package com.charrey.pruning.cached;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Domainchecker that prematurely stops the search when an AllDifferent constraint fails, proving the current search
 * path is unfruitful.
 */
public class AllDifferentPruner extends DefaultCachedPruner {

    @NotNull
    private final AllDifferent allDifferent;


    private AllDifferentPruner(AllDifferentPruner copyOf) {
        super(copyOf);
        this.allDifferent = copyOf.allDifferent;
    }

    /**
     * Instantiates a new AllDifferentPruner
     *
     * @param data utility data (for cached computation)
     */
    public AllDifferentPruner(@NotNull UtilityData data, Settings settings, GlobalOccupation occupation) {
        super(settings, data.getPatternGraph(), data.getTargetGraph(), occupation);
        this.allDifferent = new AllDifferent();
    }

    @Override
    public Pruner copy() {
        return new AllDifferentPruner(this);
    }

    @Override
    public void close() {
        //nothing needs to be closed
    }


    @Override
    public boolean isUnfruitful(int verticesPlaced, PartialMatchingProvider partialMatchingProvider) {
        return domain.subList(verticesPlaced, domain.size()).stream().anyMatch(TIntSet::isEmpty) || !allDifferent.get(domain);
    }


    @NotNull
    @Override
    public String toString() {
        TIntList[] domainString = new TIntList[domain.size()];
        for (int i = 0; i < domainString.length; i++) {
            domainString[i] = new TIntArrayList(domain.get(i));
            domainString[i].sort();
        }
        return "AllDifferentPruner{domain=" + Arrays.toString(domainString) +
                '}';
    }
}
