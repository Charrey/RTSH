package com.charrey.pathiterators.yen;

import com.charrey.occupation.GlobalOccupation;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.MaskSubgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class YenPathIterator extends PathIterator {
    @NotNull
    private final GlobalOccupation occupation;

    @NotNull
    private final MyGraph targetGraph;
    private final String init;
    private final Supplier<Integer> verticesPlaced;

    @NotNull
    private final YenShortestPathIterator<Vertex, DefaultEdge> yen;

    private OccupationTransaction transaction;
    private final Set<Vertex> occupied = new HashSet<>();

    public YenPathIterator(@NotNull MyGraph targetGraph, @NotNull Vertex tail, Vertex head, @NotNull GlobalOccupation occupation, Supplier<Integer> verticesPlaced, boolean refuseLongerPaths) {
        super(tail, head, refuseLongerPaths);
        this.targetGraph = targetGraph;
        this.occupation = occupation;
        this.transaction = occupation.getTransaction();
        init = occupation.toString();
        this.verticesPlaced = verticesPlaced;
        yen = new YenShortestPathIterator<>(new MaskSubgraph<>(targetGraph, x -> !x.equals(tail) && !x.equals(head) && occupation.isOccupied(x), y -> false), tail, head);
    }

    private Path previousPath = null;

    @Nullable
    @Override
    public Path next() {
        transaction.uncommit();
        if (previousPath != null) {
            previousPath.intermediate().forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x));
        }
        assert occupation.toString().equals(init) : "Initially: " + init + "; now: " + occupation;
        while (yen.hasNext()) {
            Path pathFound = new Path(yen.next());
            if (refuseLongerPaths && hasUnnecessarilyLongPaths(pathFound)) {
                continue;
            }
            boolean okay = true;
            for (Vertex v : pathFound.intermediate()) {
                try {
                    transaction.occupyRoutingAndCheck(verticesPlaced.get(), v);
                } catch (DomainCheckerException e) {
                    okay = false;
                    break;
                }
            }
            if (okay) {
                transaction.commit();
                previousPath = new Path(pathFound);
                return previousPath;
            }
        }
        previousPath = null;
        return null;

    }

    private boolean hasUnnecessarilyLongPaths(@NotNull Path pathFound) {
        for (int i = 0; i < pathFound.length() - 1; i++) {
            Vertex from = pathFound.get(i);
            Set<Vertex> neighbours = targetGraph.outgoingEdgesOf(from).stream().map(x -> Graphs.getOppositeVertex(targetGraph, x, from)).collect(Collectors.toUnmodifiableSet());
            List<Vertex> otherCandidates = pathFound.asList().subList(i + 2, pathFound.length());
            if (neighbours.stream().anyMatch(otherCandidates::contains)) {
                return true;
            }
        }
        return false;
    }

}
