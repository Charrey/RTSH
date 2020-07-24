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

    public SettingsBuilder() {
        settings = new Settings(
                new LabelDegreeFiltering(),
                true,
                PruningConstants.NONE,
                new KPathStrategy(),
                PruningApplicationConstants.SERIAL,
                Integer.MAX_VALUE
        );
    }

    public SettingsBuilder(Settings settings) {
        this.settings = new Settings(settings.filtering, settings.refuseLongerPaths, settings.pruningMethod, settings.pathIteration, settings.whenToApply, settings.vertexLimit);
    }

    private void setFiltering(FilteringSettings filtering) {
        if (lockFiltering) {
            throw new IllegalStateException("Filtering method has already been set to " + settings.filtering);
        }
        settings.filtering = filtering;
        lockFiltering = true;
    }

    private void setAllowLongerPaths(boolean allow) {
        if (lockLongerPaths) {
            throw new IllegalStateException("Whether to allow longer paths has already been set to " + !settings.refuseLongerPaths);
        }
        settings.refuseLongerPaths = !allow;
        lockLongerPaths = true;
    }

    private SettingsBuilder withPruningMethod(int pruningMethod) {
        if (lockPruning) {
            throw new IllegalStateException("Pruning method has already been set.");
        }
        settings.pruningMethod = pruningMethod;
        lockPruning = true;
        return this;
    }

    public SettingsBuilder withPathIteration(IteratorSettings pathIteration) {
        if (lockPathIteration) {
            throw new IllegalStateException("Path iteration method has already been set to " + pathIteration);
        }
        settings.pathIteration = pathIteration;
        lockPathIteration = true;
        return this;
    }

    private void setWhenToApply(PruningApplicationConstants whenToApply) {
        if (lockWhenToApply) {
            throw new IllegalStateException("Pruning application method has already been set");
        }
        settings.whenToApply = whenToApply;
        lockWhenToApply = true;
    }

    public SettingsBuilder withVertexLimit(int limit) {
        if (lockVertexLimit) {
            throw new IllegalStateException("Vertex limit has already been set to " + settings.vertexLimit);
        }
        settings.vertexLimit = limit;
        lockVertexLimit = true;
        return this;
    }

    public SettingsBuilder withLabelDegreeFiltering() {
        setFiltering(new LabelDegreeFiltering());
        return this;
    }

    public SettingsBuilder withoutFiltering() {
        setFiltering(new NoFiltering());
        return this;
    }

    public SettingsBuilder withUnmatchedDegreesFiltering() {
        setFiltering(new UnmatchedDegreesFiltering());
        return this;
    }

    public SettingsBuilder withMatchedReachabilityFiltering() {
        setFiltering(new MReachabilityFiltering());
        return this;
    }

    public SettingsBuilder withNeighbourReachabilityFiltering() {
        setFiltering(new NReachabilityFiltering());
        return this;
    }

    public SettingsBuilder avoidingLongerPaths() {
        setAllowLongerPaths(false);
        return this;
    }

    public SettingsBuilder allowingLongerPaths() {
        setAllowLongerPaths(true);
        return this;
    }

    public SettingsBuilder withZeroDomainPruning() {
        return withPruningMethod(PruningConstants.ZERODOMAIN);
    }

    public SettingsBuilder withAllDifferentPruning() {
        return withPruningMethod(PruningConstants.ALL_DIFFERENT);
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
        setWhenToApply(PruningApplicationConstants.SERIAL);
        return this;
    }

    public SettingsBuilder withParallelPruning() {
        setWhenToApply(PruningApplicationConstants.PARALLEL);
        return this;
    }

    public SettingsBuilder withCachedPruning() {
        setWhenToApply(PruningApplicationConstants.CACHED);
        return this;
    }


    public Settings get() {
        lockFiltering = true;
        lockLongerPaths = true;
        lockPruning = true;
        lockPathIteration = true;
        lockWhenToApply = true;
        lockVertexLimit = true;
        check();
        return settings;
    }

    private void check() {
        if (settings.whenToApply != PruningApplicationConstants.CACHED && settings.pruningMethod == PruningConstants.ALL_DIFFERENT) {
            throw new IllegalArgumentException("Alldifferent is not compatible with serial or parallel pruning.");
        }
    }


}
