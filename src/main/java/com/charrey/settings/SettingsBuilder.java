package com.charrey.settings;

import com.charrey.settings.iterator.*;
import com.charrey.settings.pruning.PruningMethod;
import com.charrey.settings.pruning.WhenToApply;
import com.charrey.settings.pruning.domainfilter.*;

import java.util.Set;

import static com.charrey.settings.pathiteration.PathIteration.DFS_ARBITRARY;
import static com.charrey.settings.pathiteration.PathIteration.DFS_GREEDY;

public class SettingsBuilder {


    private final Settings settings;

    private boolean lockFiltering = false;
    private boolean lockLongerPaths = false;
    private boolean lockPruning = false;
    private boolean lockPathIteration = false;
    private boolean lockWhenToApply = false;
    private boolean lockVertexLimit = false;
    private boolean lockPathsLimit = false;
    private boolean lockTargetVertexOrder = false;
    private boolean lockDFSCaching = false;
    private boolean lockContraction = false;

    public SettingsBuilder() {
        settings = new Settings(
                new LabelDegreeFiltering(),
                true,
                PruningMethod.NONE,
                new KPathStrategy(),
                WhenToApply.SERIAL,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE,
                TargetVertexOrder.LARGEST_DEGREE_FIRST,
                false,
                false
        );
    }

    public SettingsBuilder(Settings settings) {
        this.settings = settings.newInstance();
    }

    private SettingsBuilder setFiltering(FilteringSettings filtering) {
        if (lockFiltering) {
            throw new IllegalStateException("Filtering method has already been set to " + settings.getFiltering());
        }
        settings.setFiltering(filtering);
        lockFiltering = true;
        return this;
    }

    private SettingsBuilder setAllowLongerPaths(boolean allow) {
        if (lockLongerPaths) {
            throw new IllegalStateException("Whether to allow longer paths has already been set to " + !settings.getRefuseLongerPaths());
        }
        settings.setRefuseLongerPaths(!allow);
        lockLongerPaths = true;
        return this;
    }

    private SettingsBuilder setTargetVertexOrder(TargetVertexOrder order) {
        if (lockTargetVertexOrder) {
            throw new IllegalStateException("Target vertex order has already been set to " + settings.getTargetVertexOrder());
        }
        settings.setTargetVertexOrder(order);
        lockTargetVertexOrder = true;
        return this;
    }

    private SettingsBuilder withPruningMethod(PruningMethod pruningMethod) {
        if (lockPruning) {
            throw new IllegalStateException("Pruning method has already been set.");
        }
        settings.setPruningMethod(pruningMethod);
        lockPruning = true;
        return this;
    }

    public SettingsBuilder withPathIteration(IteratorSettings pathIteration) {
        if (lockPathIteration) {
            throw new IllegalStateException("Path iteration method has already been set to " + pathIteration);
        }
        settings.setPathIteration(pathIteration);
        lockPathIteration = true;
        return this;
    }

    private SettingsBuilder setWhenToApply(WhenToApply whenToApply) {
        if (lockWhenToApply) {
            throw new IllegalStateException("Pruning application method has already been set");
        }
        settings.setWhenToApply(whenToApply);
        lockWhenToApply = true;
        return this;
    }

    public SettingsBuilder withVertexLimit(int limit) {
        if (lockVertexLimit) {
            throw new IllegalStateException("Vertex limit has already been set to " + settings.getVertexLimit());
        }
        settings.setVertexLimit(limit);
        lockVertexLimit = true;
        return this;
    }

    public SettingsBuilder withPathsLimit(int limit) {
        if (lockPathsLimit) {
            throw new IllegalStateException("Path limit has already been set to " + settings.getPathsLimit());
        }
        settings.setPathsLimit(limit);
        lockPathsLimit = true;
        return this;
    }

    private SettingsBuilder setDFSCaching(boolean dfsCaching) {
        if (lockDFSCaching) {
            throw new IllegalStateException("DFS caching has already been set to " + settings.getDfsCaching());
        }
        settings.setPathIterationCaching(dfsCaching);
        lockDFSCaching = true;
        return this;
    }

    private SettingsBuilder setContraction(boolean contraction) {
        if (lockContraction) {
            throw new IllegalStateException("Contraction has already been set to " + settings.getContraction());
        }
        settings.setContraction(contraction);
        lockContraction = true;
        return this;
    }


    public SettingsBuilder withLargestDegreeFirstTargetVertexOrder() {
        return setTargetVertexOrder(TargetVertexOrder.LARGEST_DEGREE_FIRST);
    }

    public SettingsBuilder withClosestTargetVertexOrder() {
        return setTargetVertexOrder(TargetVertexOrder.CLOSEST_TO_MATCHED);
    }

    public SettingsBuilder withClosestTargetVertexOrderCached() {
        return setTargetVertexOrder(TargetVertexOrder.CLOSEST_TO_MATCHED_CACHED);
    }

    public SettingsBuilder withLabelDegreeFiltering() {
        return setFiltering(new LabelDegreeFiltering());
    }

    public SettingsBuilder withoutFiltering() {
        return setFiltering(new NoFiltering());
    }

    public SettingsBuilder withUnmatchedDegreesFiltering() {
        return setFiltering(new UnmatchedDegreesFiltering());
    }

    public SettingsBuilder withMatchedReachabilityFiltering() {
        return setFiltering(new MReachabilityFiltering());
    }

    public SettingsBuilder withNeighbourReachabilityFiltering(int level) {
        return setFiltering(new NReachabilityFiltering(level));
    }

    public SettingsBuilder withNeighbourReachabilityFiltering() {
        return setFiltering(new NReachabilityFiltering(Integer.MAX_VALUE));
    }

    public SettingsBuilder avoidingLongerPaths() {
        return setAllowLongerPaths(false);
    }

    public SettingsBuilder allowingLongerPaths() {
        return setAllowLongerPaths(true);
    }

    public SettingsBuilder withZeroDomainPruning() {
        return withPruningMethod(PruningMethod.ZERODOMAIN);
    }

    public SettingsBuilder withAllDifferentPruning() {
        return withPruningMethod(PruningMethod.ALLDIFFERENT);
    }

    public SettingsBuilder withoutPruning() {
        if (lockWhenToApply) {
            throw new IllegalStateException("Pruning application may not be set when disabling pruning.");
        }
        lockWhenToApply = true;
        return withPruningMethod(PruningMethod.NONE);
    }

    public SettingsBuilder withKPathRouting() {
        return withPathIteration(new KPathStrategy());
    }

    public SettingsBuilder withCachedDFSRouting() {
        return withPathIteration(new DFSStrategy()).setDFSCaching(true);
    }

    public SettingsBuilder withInplaceDFSRouting() {
        return withPathIteration(new DFSStrategy()).setDFSCaching(false);
    }

    public SettingsBuilder withCachedGreedyDFSRouting() {
        return withPathIteration(new OldGreedyDFSStrategy()).setDFSCaching(true);
    }

    public SettingsBuilder withInplaceOldGreedyDFSRouting() {
        return withPathIteration(new OldGreedyDFSStrategy()).setDFSCaching(false);
    }

    public SettingsBuilder withInplaceNewGreedyDFSRouting() {
        return withPathIteration(new NewGreedyDFSStrategy()).setDFSCaching(false);
    }

    public SettingsBuilder withControlPointRouting() {
        return withPathIteration(new ControlPointIteratorStrategy(Integer.MAX_VALUE));
    }

    public SettingsBuilder withControlPointRouting(int maxControlPoints) {
        return withPathIteration(new ControlPointIteratorStrategy(maxControlPoints));
    }

    public SettingsBuilder withSerialPruning() {
        return setWhenToApply(WhenToApply.SERIAL);
    }

    public SettingsBuilder withParallelPruning() {
        return setWhenToApply(WhenToApply.PARALLEL);
    }

    public SettingsBuilder withCachedPruning() {
        return setWhenToApply(WhenToApply.CACHED);
    }

    public SettingsBuilder withContraction() {
        return setContraction(true);
    }

    public SettingsBuilder withoutContraction() {
        return setContraction(false);
    }


    public Settings get() {
        if (lockDFSCaching && !(Set.of(DFS_GREEDY, DFS_ARBITRARY).contains(settings.getPathIteration().iterationStrategy))) {
            throw new IllegalStateException("DFS caching method has been set without specifying DFS as path iteration strategy.");
        }
        lockFiltering = true;
        lockLongerPaths = true;
        lockPruning = true;
        lockPathIteration = true;
        lockWhenToApply = true;
        lockVertexLimit = true;
        lockPathsLimit = true;
        lockTargetVertexOrder = true;
        lockDFSCaching = true;
        lockContraction = true;
        if (settings.getFiltering() instanceof MReachabilityFiltering && settings.getWhenToApply() == WhenToApply.CACHED) {
            ((MReachabilityFiltering)settings.getFiltering()).setCached();
        }
        check();
        return settings;
    }

    private void check() {
        if (settings.getWhenToApply() != WhenToApply.CACHED && settings.getPruningMethod() == PruningMethod.ALLDIFFERENT) {
            throw new IllegalArgumentException("Alldifferent is not compatible with serial or parallel pruning.");
        }
        if (settings.getPathsLimit() < 1) {
            throw new IllegalArgumentException("Paths limit must be greater than 0.");
        }
        if (settings.getVertexLimit() < 1) {
            throw new IllegalArgumentException("Paths limit must be greater than 0.");
        }
    }



}
