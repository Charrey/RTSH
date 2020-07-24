package com.charrey.settings;

import com.charrey.settings.iterator.IteratorSettings;
import com.charrey.settings.pruning.PruningApplicationConstants;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Class to encapsulate the different settings under which homeomorphisms may be found. Each setting influences the
 * behaviour of the search in some way.
 */
public class Settings {

    /**
     * Whether to refuse paths that take up unnecessarily many resources.
     */
    private boolean refuseLongerPaths;
    /**
     * Which pruning method to use (select from PruningConstants.java)
     */
    private int pruningMethod;
    /**
     * Which method to iterate paths is used (select from PathIterationConstants.java)
     */
    private IteratorSettings pathIteration;

    /**
     * Which method used to filter domains is used.
     */
    private FilteringSettings filtering;

    private PruningApplicationConstants whenToApply;
    private int vertexLimit;

    /**
     * Instantiates a new Settings.
     *
     * @param refuseLongerPaths Whether to refuse paths that take up unnecessarily many resources.
     * @param pruningMethod     Which pruning method to use (select from PruningConstants.java)
     * @param pathIteration     Which method to iterate paths is used (select from PathIterationConstants.java)
     */
    Settings(FilteringSettings filtering,
             boolean refuseLongerPaths,
             int pruningMethod,
             IteratorSettings pathIteration,
             PruningApplicationConstants whenToApply,
             int vertexLimit) {
        this.filtering = filtering;
        this.refuseLongerPaths = refuseLongerPaths;
        this.pruningMethod = pruningMethod;
        this.pathIteration = pathIteration;
        this.whenToApply = whenToApply;
        this.vertexLimit = vertexLimit;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return filtering.equals(settings.filtering) &&
                refuseLongerPaths == settings.refuseLongerPaths &&
                pruningMethod == settings.pruningMethod &&
                pathIteration.equals(settings.pathIteration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filtering, refuseLongerPaths, pruningMethod, pathIteration);
    }

    public PruningApplicationConstants getWhenToApply() {
        return whenToApply;
    }

    void setWhenToApply(PruningApplicationConstants whenToApply) {
        this.whenToApply = whenToApply;
    }

    public int getPruningMethod() {
        return pruningMethod;
    }

    void setPruningMethod(int pruningMethod) {
        this.pruningMethod = pruningMethod;
    }

    public FilteringSettings getFiltering() {
        return filtering;
    }

    void setFiltering(FilteringSettings filtering) {
        this.filtering = filtering;
    }

    public int getVertexLimit() {
        return vertexLimit;
    }

    void setVertexLimit(int vertexLimit) {
        this.vertexLimit = vertexLimit;
    }

    public IteratorSettings getPathIteration() {
        return pathIteration;
    }

    void setPathIteration(IteratorSettings pathIteration) {
        this.pathIteration = pathIteration;
    }

    public boolean getRefuseLongerPaths() {
        return refuseLongerPaths;
    }

    void setRefuseLongerPaths(boolean refuseLongerPaths) {
        this.refuseLongerPaths = refuseLongerPaths;
    }
}
