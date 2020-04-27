package com.charrey;

import com.charrey.graph.Vertex;

public interface OccupationListener {


    void afterReleaseRouting(Vertex v);

    void afterReleaseVertex(Vertex v);

    void afterOccupyRouting(Vertex v);

    void afterOccupyVertex(Vertex v);
}
