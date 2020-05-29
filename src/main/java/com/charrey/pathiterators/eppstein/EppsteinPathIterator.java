package com.charrey.pathiterators.eppstein;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.PathIterator;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.EppsteinShortestPathIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.nio.dot.DOTExporter;

import java.util.function.Supplier;

public class EppsteinPathIterator extends PathIterator {
    private final Occupation occupation;
    private final MyGraph targetGraph;

    private EppsteinShortestPathIterator<Vertex, DefaultEdge> eppstein;

    public EppsteinPathIterator(MyGraph targetGraph, Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> placementSize) {
        super(targetGraph.vertexSet().size(), tail, head);
        this.targetGraph = targetGraph;
        this.occupation = occupation;
        eppstein = new EppsteinShortestPathIterator<>(
                new MaskSubgraph<>(targetGraph, x -> !x.equals(tail) && !x.equals(head) && occupation.isOccupied(x), y -> false), tail, head);
    }

    @Override
    public void reset() {
        eppstein = new EppsteinShortestPathIterator<>(new MaskSubgraph<>(targetGraph, x -> !x.equals(tail()) && !x.equals(head()) && occupation.isOccupied(x), y -> false), tail(), head());
    }

    @Override
    public Path next() {
        if (eppstein.hasNext()) {
            DOTExporter<Vertex, DefaultEdge> exporter = new DOTExporter<>();
            exporter.setVertexIdProvider(vertex -> String.valueOf(vertex.data()));
            GraphPath<Vertex, DefaultEdge> pathFound = eppstein.next();
            return pathFound == null ? null : new Path(pathFound);
        } else {
            return null;
        }
    }

    @Override
    public Object getState() {
        throw new UnsupportedOperationException();
    }
}
