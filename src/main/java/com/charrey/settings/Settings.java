package com.charrey.settings;

import com.charrey.settings.iterator.IteratorSettings;
import com.charrey.settings.pruning.WhenToApply;
import com.charrey.settings.pruning.PruningMethod;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Class to encapsulate the different settings under which homeomorphisms may be found. Each setting influences the
 * behaviour of the search in some way.
 */
public class Settings implements Cloneable {

    private boolean contraction;
    private boolean dfsCaching;
    /**
     * Whether to refuse paths that take up unnecessarily many resources.
     */
    private boolean refuseLongerPaths;
    /**
     * Which pruning method to use (select from PruningConstants.java)
     */
    private PruningMethod pruningMethod;
    /**
     * Which method to iterate paths is used (select from PathIterationConstants.java)
     */
    private IteratorSettings pathIteration;

    /**
     * Which method used to filter domains is used.
     */
    private FilteringSettings filtering;

    private WhenToApply whenToApply;
    private int vertexLimit;
    private TargetVertexOrder targetVertexOrder;

    /**
     * Instantiates a new Settings.
     *
     * @param refuseLongerPaths Whether to refuse paths that take up unnecessarily many resources.
     * @param pruningMethod     Which pruning method to use (select from PruningConstants.java)
     * @param pathIteration     Which method to iterate paths is used (select from PathIterationConstants.java)
     */
    Settings(FilteringSettings filtering,
             boolean refuseLongerPaths,
             PruningMethod pruningMethod,
             IteratorSettings pathIteration,
             WhenToApply whenToApply,
             int vertexLimit,
             TargetVertexOrder targetVertexOrder,
             boolean dfsCaching,
             boolean contraction) {
        this.filtering = filtering;
        this.refuseLongerPaths = refuseLongerPaths;
        this.pruningMethod = pruningMethod;
        this.pathIteration = pathIteration;
        this.whenToApply = whenToApply;
        this.vertexLimit = vertexLimit;
        this.targetVertexOrder = targetVertexOrder;
        this.dfsCaching = dfsCaching;
        this.contraction = contraction;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return filtering.equals(settings.filtering) &&
                refuseLongerPaths == settings.refuseLongerPaths &&
                pruningMethod == settings.pruningMethod &&
                pathIteration.equals(settings.pathIteration) &&
                dfsCaching == settings.dfsCaching &&
                contraction == settings.contraction;
    }

    @Override
    public Object clone() {
        Settings toReturn;
        try {
            toReturn = (Settings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException();
        }
        toReturn.pathIteration = (IteratorSettings) pathIteration.clone();
        return toReturn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filtering, refuseLongerPaths, pruningMethod, pathIteration, dfsCaching, contraction);
    }

    public WhenToApply getWhenToApply() {
        return whenToApply;
    }

    void setWhenToApply(WhenToApply whenToApply) {
        this.whenToApply = whenToApply;
    }

    public PruningMethod getPruningMethod() {
        return pruningMethod;
    }

    void setPruningMethod(PruningMethod pruningMethod) {
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

    public TargetVertexOrder getTargetVertexOrder() {
        return targetVertexOrder;
    }

    void setTargetVertexOrder(TargetVertexOrder order) {
        this.targetVertexOrder = order;
    }

    public boolean getDfsCaching() {
        return dfsCaching;
    }

    void setPathIterationCaching(boolean dfsCaching) {
        this.dfsCaching = dfsCaching;
    }

    public boolean getContraction() {
        return contraction;
    }

    void setContraction(boolean contraction) {
        this.contraction = contraction;
    }
}
