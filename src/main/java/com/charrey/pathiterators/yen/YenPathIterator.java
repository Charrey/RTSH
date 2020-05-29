package com.charrey.pathiterators.yen;

import com.charrey.Occupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.pathiterators.PathIterator;
import com.charrey.util.datastructures.checker.DomainCheckerException;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class YenPathIterator extends PathIterator {
    private final Occupation occupation;
    private final MyGraph targetGraph;
    private final String init;
    private final Supplier<Integer> verticesPlaced;

    private YenShortestPathIterator<Vertex, DefaultEdge> yen;
    private Set<Vertex> occupied = new HashSet<>();

    public YenPathIterator(MyGraph targetGraph, Vertex tail, Vertex head, Occupation occupation, Supplier<Integer> verticesPlaced) {
        super(targetGraph.vertexSet().size(), tail, head);
        this.targetGraph = targetGraph;
        this.occupation = occupation;
        init = occupation.toString();
        this.verticesPlaced = verticesPlaced;
        yen = new YenShortestPathIterator<>(new MaskSubgraph<>(targetGraph, x -> !x.equals(tail) && !x.equals(head) && occupation.isOccupied(x), y -> false), tail, head);
    }

    @Override
    public void reset() {
        yen = new YenShortestPathIterator<>(new MaskSubgraph<>(targetGraph, x -> !x.equals(tail()) && !x.equals(head()) && occupation.isOccupied(x), y -> false), tail(), head());
    }

    @Override
    public Path next() {
        occupied.forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
        occupied.clear();
        assert occupation.toString().equals(init) : "Initially: " + init + "; now: " + occupation;
        while (yen.hasNext()) {
            Path pathFound = new Path(yen.next());
            boolean okay = true;
            for (Vertex v : pathFound.intermediate()) {
                try {
                    occupation.occupyRoutingAndCheck(verticesPlaced.get(), v, pathFound);
                    occupied.add(v);
                } catch (DomainCheckerException e) {
                    occupied.forEach(x -> occupation.releaseRouting(verticesPlaced.get(), x));
                    occupied.clear();
                    okay = false;
                    break;
                }
            }
            if (okay) {
                return new Path(pathFound);
            }
        }
        assert occupied.isEmpty();
        return null;

    }

    @Override
    public Object getState() {
        throw new UnsupportedOperationException();
    }
}
