package com.charrey.pruning;

import com.charrey.runtimecheck.DomainCheckerException;

/**
 * Domain checker class that performs no pruning.
 */
@SuppressWarnings("RedundantThrows")
public class NoPruner extends Pruner {
    @Override
    public int serialized() {
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        return getClass().equals(o.getClass());
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int released) {

    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int released) {

    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int occupied) throws DomainCheckerException {

    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, int occupied) throws DomainCheckerException {

    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int occupied) {

    }

    @Override
    public boolean isUnfruitful(int verticesPlaced) {
        return false;
    }

    @Override
    public Pruner copy() {
        return this;
    }

}
