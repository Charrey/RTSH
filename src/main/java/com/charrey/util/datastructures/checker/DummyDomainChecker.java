package com.charrey.util.datastructures.checker;

import com.charrey.Occupation;
import com.charrey.graph.Vertex;

public class DummyDomainChecker extends DomainChecker {
    @Override
    public void afterReleaseVertex(Occupation occupation, int verticesPlaced, Vertex released) {

    }

    @Override
    public void afterReleaseEdge(Occupation occupation, int verticesPlaced, Vertex released) {

    }

    @Override
    public void beforeOccupyVertex(Occupation occupation, int verticesPlaced, Vertex occupied) throws DomainCheckerException {

    }

    @Override
    public void afterOccupyEdge(Occupation occupation, int verticesPlaced, Vertex occupied) throws DomainCheckerException {

    }

    @Override
    public boolean checkOK(int verticesPlaced) {
        return true;
    }
}
