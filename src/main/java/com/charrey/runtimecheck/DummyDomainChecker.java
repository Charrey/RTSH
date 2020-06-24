package com.charrey.runtimecheck;

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
    public boolean checkOK(int verticesPlaced) {
        return true;
    }

    @Override
    public DomainChecker copy() {
        return this;
    }

}
