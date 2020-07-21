package com.charrey.pruning;

import com.charrey.algorithms.UtilityData;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.NotNull;

/**
 * Domain checker class that prunes the search space if all target graph vertices in some source graph vertex' domain
 * have been used up by other matchings.
 */
public class CachedZeroDomainPruner extends DefaultCachedPruner {

    private CachedZeroDomainPruner(CachedZeroDomainPruner copyFrom) {
        super(copyFrom);
    }

    /**
     * Instantiates a new CachedZeroDomainPruner
     *
     * @param data utility data (for cached computation)
     */
    public CachedZeroDomainPruner(@NotNull UtilityData data, FilteringSettings filteringSettings, String name, GlobalOccupation occupation) {
        super(filteringSettings, data.getPatternGraph(), data.getTargetGraph(), occupation, true);
    }

    @Override
    public Pruner copy() {
        return new CachedZeroDomainPruner(this);
    }


    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return domain.subList(verticesPlaced, domain.size()).stream().anyMatch(TIntSet::isEmpty);
    }


    @NotNull
    @Override
    public String toString() {
        return "CachedZeroDomainPruner{" + "domain=" + domain + '}';
    }
}
