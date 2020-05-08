package com.charrey.util.datastructures.checker;

import com.charrey.Occupation;
import com.charrey.graph.Vertex;

public abstract class DomainChecker {

    public abstract void afterReleaseVertex(Occupation occupation, int verticesPlaced, Vertex released);

    public abstract void afterReleaseEdge(Occupation occupation, int verticesPlaced, Vertex released);

    public abstract void beforeOccupyVertex(Occupation occupation, int verticesPlaced, Vertex occupied) throws DomainCheckerException;

    public abstract void afterOccupyEdge(Occupation occupation, int verticesPlaced, Vertex occupied) throws DomainCheckerException;

    public abstract boolean checkOK(int verticesPlaced);
}
