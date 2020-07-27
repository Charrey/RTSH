package com.charrey.pruning;

/**
 * Domain checker class that performs no pruning.
 */
@SuppressWarnings("RedundantThrows")
public class NoPruner extends Pruner {

    public NoPruner() {
        super(null, null, null, null);
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int released) {
        //do nothing
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int released) {
        //do nothing
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
        //do nothing
    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int occupied, PartialMatching partialMatching) throws DomainCheckerException {
        //do nothing
    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int occupied) {
        //do nothing
    }

    @Override
    public boolean isUnfruitfulCached(int verticesPlaced) {
        return false;
    }

    @Override
    public Pruner copy() {
        return this;
    }

    @Override
    public void close() {
        //do nothing
    }

    @Override
    public void checkPartial(PartialMatching partialMatching) throws DomainCheckerException {
        //do nothing
    }

}
