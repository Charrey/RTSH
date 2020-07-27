package com.charrey.settings;

import com.charrey.settings.iterator.*;
import com.charrey.settings.pruning.PruningApplicationConstants;
import com.charrey.settings.pruning.PruningConstants;
import com.charrey.settings.pruning.domainfilter.*;

public class SettingsBuilder {


    private final Settings settings;

    private boolean lockFiltering = false;
    private boolean lockLongerPaths = false;
    private boolean lockPruning = false;
    private boolean lockPathIteration = false;
    private boolean lockWhenToApply = false;
    private boolean lockVertexLimit = false;
    private boolean lockTargetVertexOrder = false;

    public SettingsBuilder() {
        settings = new Settings(
                new LabelDegreeFiltering(),
                true,
                PruningConstants.NONE,
                new KPathStrategy(),
                PruningApplicationConstants.SERIAL,
                Integer.MAX_VALUE,
                TargetVertexOrder.LARGEST_DEGREE_FIRST
        );
    }

    public SettingsBuilder(Settings settings) {
        this.settings = new Settings(settings.getFiltering(), settings.getRefuseLongerPaths(), settings.getPruningMethod(), settings.getPathIteration(), settings.getWhenToApply(), settings.getVertexLimit(), settings.getTargetVertexOrder());
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

    private SettingsBuilder withPruningMethod(PruningConstants pruningMethod) {
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

    private SettingsBuilder setWhenToApply(PruningApplicationConstants whenToApply) {
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

    public SettingsBuilder withLargestDegreeFirstTargetVertexOrder() {
        return setTargetVertexOrder(TargetVertexOrder.LARGEST_DEGREE_FIRST);
    }

    public SettingsBuilder withClosestTargetVertexOrder() {
        return setTargetVertexOrder(TargetVertexOrder.CLOSEST_TO_MATCHED);
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

    public SettingsBuilder withNeighbourReachabilityFiltering() {
        return setFiltering(new NReachabilityFiltering());
    }

    public SettingsBuilder avoidingLongerPaths() {
        return setAllowLongerPaths(false);
    }

    public SettingsBuilder allowingLongerPaths() {
        return setAllowLongerPaths(true);
    }

    public SettingsBuilder withZeroDomainPruning() {
        return withPruningMethod(PruningConstants.ZERODOMAIN);
    }

    public SettingsBuilder withAllDifferentPruning() {
        return withPruningMethod(PruningConstants.ALLDIFFERENT);
    }

    public SettingsBuilder withoutPruning() {
        if (lockWhenToApply) {
            throw new IllegalStateException("Pruning application may not be set when disabling pruning.");
        }
        lockWhenToApply = true;
        return withPruningMethod(PruningConstants.NONE);
    }

    public SettingsBuilder withKPathRouting() {
        return withPathIteration(new KPathStrategy());
    }

    public SettingsBuilder withDFSRouting() {
        return withPathIteration(new DFSStrategy());
    }

    public SettingsBuilder withGreedyDFSRouting() {
        return withPathIteration(new GreedyDFSStrategy());
    }

    public SettingsBuilder withControlPointRouting() {
        return withPathIteration(new ControlPointIteratorStrategy(Integer.MAX_VALUE));
    }

    public SettingsBuilder withControlPointRouting(int maxControlPoints) {
        return withPathIteration(new ControlPointIteratorStrategy(maxControlPoints));
    }

    public SettingsBuilder withSerialPruning() {
        return setWhenToApply(PruningApplicationConstants.SERIAL);
    }

    public SettingsBuilder withParallelPruning() {
        return setWhenToApply(PruningApplicationConstants.PARALLEL);
    }

    public SettingsBuilder withCachedPruning() {
        return setWhenToApply(PruningApplicationConstants.CACHED);
    }


    public Settings get() {
        lockFiltering = true;
        lockLongerPaths = true;
        lockPruning = true;
        lockPathIteration = true;
        lockWhenToApply = true;
        lockVertexLimit = true;
        lockTargetVertexOrder = true;
        check();
        return settings;
    }

    private void check() {
        if (settings.getWhenToApply() != PruningApplicationConstants.CACHED && settings.getPruningMethod() == PruningConstants.ALLDIFFERENT) {
            throw new IllegalArgumentException("Alldifferent is not compatible with serial or parallel pruning.");
        }
    }


}
