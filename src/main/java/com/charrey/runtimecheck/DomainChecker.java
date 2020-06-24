package com.charrey.runtimecheck;

public abstract class DomainChecker {

    public abstract void afterReleaseVertex(int verticesPlaced, int released);

    public abstract void afterReleaseEdge(int verticesPlaced, int released);

    public abstract void beforeOccupyVertex(int verticesPlaced, int occupied) throws DomainCheckerException;

    public abstract void afterOccupyEdge(int verticesPlaced, int occupied) throws DomainCheckerException;

    public abstract boolean checkOK(int verticesPlaced);

    public abstract DomainChecker copy();
}
