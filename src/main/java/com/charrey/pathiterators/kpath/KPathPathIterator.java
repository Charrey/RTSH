package com.charrey.pathiterators.kpath;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.occupation.OccupationTransaction;
import com.charrey.pathiterators.PathIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.graph.MaskSubgraph;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This pathiterator iterates from shortest path to longest path (i.e. solves the K-Path problem).
 */
public class KPathPathIterator extends PathIterator {
    @NotNull
    private final GlobalOccupation occupation;

    @NotNull
    private final MyGraph targetGraph;
    private final String init;
    private final Supplier<Integer> verticesPlaced;

    @NotNull
    private final YenShortestPathIterator<Integer, MyEdge> yen;

    private final OccupationTransaction transaction;
    private int counter = 0;

    /**
     * Instantiates a new K-Path iterator.
     *
     * @param targetGraph       the target graph
     * @param tail              the source of the path
     * @param head              the target of the path
     * @param occupation        the GlobalOccupation where intermediate nodes are registered
     * @param verticesPlaced    supplier of the number of source graph vertices placed at this point in the search
     * @param refuseLongerPaths whether to refuse paths that use unnecessarily many resources.
     */
    public KPathPathIterator(@NotNull MyGraph targetGraph, int tail, int head, @NotNull GlobalOccupation occupation, Supplier<Integer> verticesPlaced, boolean refuseLongerPaths) {
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
            Path pathFound = new Path(targetGraph, yen.next());
            if (refuseLongerPaths && hasUnnecessarilyLongPaths(pathFound)) {
                continue;
            }
            boolean okay = true;
            for (int v : pathFound.intermediate()) {
                try {
                    transaction.occupyRoutingAndCheck(verticesPlaced.get(), v);
                } catch (DomainCheckerException e) {
                    okay = false;
                    break;
                }
            }
            if (okay) {
                try {
                    transaction.commit();
                } catch (DomainCheckerException e) {
                    return next();
                }
                previousPath = new Path(pathFound);
                counter++;
                return previousPath;
            }
        }
        previousPath = null;
        return null;

    }

    @Override
    public String debugInfo() {
        return String.valueOf(counter);
    }

    private boolean hasUnnecessarilyLongPaths(@NotNull Path pathFound) {
        for (int i = 0; i < pathFound.length() - 1; i++) {
            int from = pathFound.get(i);
            Set<Integer> neighbours = targetGraph.outgoingEdgesOf(from).stream().map(x -> Graphs.getOppositeVertex(targetGraph, x, from)).collect(Collectors.toUnmodifiableSet());
            List<Integer> otherCandidates = pathFound.asList().subList(i + 2, pathFound.length());
            if (neighbours.stream().anyMatch(otherCandidates::contains)) {
                return true;
            }
        }
        return false;
    }

}
