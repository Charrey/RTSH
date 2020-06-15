package com.charrey.runtimecheck;

import com.charrey.graph.Vertex;

@SuppressWarnings("RedundantThrows")
public class DummyDomainChecker extends DomainChecker {
    @Override
    public void afterReleaseVertex(int verticesPlaced, Vertex released) {

    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, Vertex released) {

    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, Vertex occupied) throws DomainCheckerException {

    }

    @Override
    public void afterOccupyEdge(int verticesPlaced, Vertex occupied) throws DomainCheckerException {

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
