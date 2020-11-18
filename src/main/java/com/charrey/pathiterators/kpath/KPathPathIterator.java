package com.charrey.pathiterators.kpath;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.util.Util;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.graph.MaskSubgraph;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.charrey.algorithms.RefuseLongerPaths.hasUnnecessarilyLongPaths;

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

    private int counter = 0;

    /**
     * Instantiates a new K-Path iterator.
     *
     * @param targetGraph       the target graph
     * @param tail              the source of the path
     * @param head              the target of the path
     * @param occupation        the GlobalOccupation where intermediate nodes are registered
     * @param verticesPlaced    supplier of the number of source graph vertices placed at this point in the search
     */
    public KPathPathIterator(@NotNull MyGraph targetGraph,
                             Settings settings, int tail,
                             int head,
                             @NotNull GlobalOccupation occupation,
                             Supplier<Integer> verticesPlaced,
                             PartialMatchingProvider partialMatchingProvider,
                             long timeoutTime, int cripple) {
        super(targetGraph, tail, head, settings, occupation, occupation.getTransaction(), partialMatchingProvider, timeoutTime, verticesPlaced, cripple);
        this.targetGraph = targetGraph;
        this.occupation = occupation;
        init = occupation.toString();
        this.verticesPlaced = verticesPlaced;
        yen = new YenShortestPathIterator<>(new MaskSubgraph<>(targetGraph, x -> !x.equals(tail) && !x.equals(head) && occupation.isOccupied(x), y -> false), tail, head);
    }

    private Path previousPath = null;

    @Override
    public TIntSet getLocallyOccupied() {
        return Util.emptyTIntSet;
    }

    @Nullable
    @Override
    public Path getNext() {
        transaction.uncommit(verticesPlaced.get(), this::getPartialMatching);
        if (previousPath != null) {
            previousPath.intermediate().forEach(x -> transaction.releaseRouting(verticesPlaced.get(), x, this::getPartialMatching));
        }
        assert occupation.toString().equals(init) : "Initially: " + init + "; now: " + occupation;
        while (yen.hasNext()) {
            if (Thread.currentThread().isInterrupted() || System.currentTimeMillis() >= timeoutTime) {
                return null;
            }
            Path pathFound = new Path(targetGraph, yen.next());
            if (refuseLongerPaths && hasUnnecessarilyLongPaths(targetGraph, pathFound)) {
                continue;
            }
            try {
                transaction.occupyRoutingAndCheck(verticesPlaced.get(), pathFound, this::getPartialMatching);
            } catch (DomainCheckerException e) {
                continue;
            }
            try {
                transaction.commit(verticesPlaced.get(), this::getPartialMatching);
            } catch (DomainCheckerException e) {
                return next();
            }
            previousPath = new Path(pathFound);
            counter++;
            return previousPath;
        }
        previousPath = null;
        return null;

    }


    @Override
    public String debugInfo() {
        return String.valueOf(counter);
    }

//    private boolean hasUnnecessarilyLongPaths(@NotNull Path pathFound) {
//        for (int i = 0; i < pathFound.length() - 2; i++) {
//            int from = pathFound.get(i);
//            int head = pathFound.get(i+1);
//            TIntList to = new TIntArrayList();
//            pathFound.asList().subList(i + 2, pathFound.length()).forEach(new TIntProcedure() {
//                boolean inPrePhase = true;
//
//                @Override
//                public boolean execute(int value) {
//                    if (inPrePhase && targetGraph.degreeOf(value) == 2) {
//                        return true;
//                    } else {
//                        inPrePhase = false;
//                        to.add(value);
//                        return true;
//                    }
//                }
//            });
//            if (canReachThroughIsolatedPath(targetGraph, from, to, head)) {
//                return true;
//            }
//        }
//        return false;
//    }


}
