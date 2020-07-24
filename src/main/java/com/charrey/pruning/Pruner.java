package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.settings.Settings;

public abstract class Pruner {

    protected final Settings settings;
    protected final MyGraph targetGraph;
    protected final MyGraph sourceGraph;
    protected final GlobalOccupation occupation;

    public Pruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, GlobalOccupation occupation) {
        this.settings = settings;
        this.sourceGraph = sourceGraph;
        this.targetGraph = targetGraph;
        this.occupation = occupation;
    }

    /**
     * This method is called after a target graph vertex used in vertex-on-vertex matching is released.
     *
     * @param verticesPlaced the number of placed source graph vertices
     * @param released       the released vertex
     */
    public abstract void afterReleaseVertex(int verticesPlaced, int released);

    /**
     * This method is called after a target graph vertex used as intermediate vertex is released.
     *
     * @param verticesPlaced the number of placed source graph vertices
     * @param released       the released vertex
     */
    public abstract void afterReleaseEdge(int verticesPlaced, int released);

    /**
     * This method is called just before a new target graph vertex is used in vertex-on-vertex matching.
     *
     * @param verticesPlaced the number of placed source graph vertices
     * @param occupied       the target graph vertex to be matched
     * @throws DomainCheckerException thrown when this occupation would provably result in an unfruitful search path
     */
    public abstract void beforeOccupyVertex(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException;

    /**
     * This method is called just before a new target graph vertex is used as intermediate vertex in a path.
     *
     * @param verticesPlaced the number of placed source graph vertices
     * @param occupied       the target graph vertex to be used
     * @throws DomainCheckerException thrown when this occupation would provably result in an unfruitful search path
     */
    public abstract void afterOccupyEdge(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException;

    /**
     * This method is called just before a new target graph vertex is used as intermediate vertex in a path. This
     * assumes that the pruning check has already been done elsewhere in the program, and is for performance reasons
     * omitted.
     *
     * @param verticesPlaced the number of placed source graph vertices
     * @param occupied       the target graph vertex to be used
     */
    public abstract void afterOccupyEdgeWithoutCheck(int verticesPlaced, int occupied);

    /**
     * Returns true if the domainchecker can prove that the current matching is unfruitful.
     *
     * @param verticesPlaced the number of placed source graph vertices
     * @return true if the domainchecker can prove that the current matching is unfruitful, or false if it cannot.
     */
    public abstract boolean isUnfruitfulCached(int verticesPlaced);

    /**
     * Provides a deep copy of this checker.
     *
     * @return A deep copy of this domainchecker
     */
    public abstract Pruner copy();

    public abstract void close();

    public abstract void checkPartial(PartialMatching partialMatching) throws DomainCheckerException;
}
