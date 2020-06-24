package com.charrey.occupation;

/**
 * Abstract class for things that keep track of some occupation of vertices.
 */
public abstract class AbstractOccupation {

    /**
     * Whether some vertex is occupied
     *
     * @param vertex the vertex to query
     * @return whether that vertex is occupied
     */
    public abstract boolean isOccupied(int vertex);
}
