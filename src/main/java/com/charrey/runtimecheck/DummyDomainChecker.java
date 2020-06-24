package com.charrey.runtimecheck;

/**
 * Domain checker class that performs no pruning.
 */
@SuppressWarnings("RedundantThrows")
public class DummyDomainChecker extends DomainChecker {
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
    public DomainChecker copy() {
        return this;
    }

}
