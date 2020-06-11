package com.charrey.util.datastructures.checker;

import com.charrey.graph.Vertex;

public abstract class DomainChecker {

    public abstract void afterReleaseVertex(int verticesPlaced, Vertex released);

    public abstract void afterReleaseEdge(int verticesPlaced, Vertex released);

    public abstract void beforeOccupyVertex(int verticesPlaced, Vertex occupied) throws DomainCheckerException;

    public abstract void afterOccupyEdge(int verticesPlaced, Vertex occupied) throws DomainCheckerException;

    public abstract boolean checkOK(int verticesPlaced);

}
