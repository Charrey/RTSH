package com.charrey.pruning.cached;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.domainfilter.MReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.NReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;
import com.charrey.util.Util;
import com.charrey.util.datastructures.MultipleKeyMap;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public abstract class MReachCachedPruner extends Pruner {

    final LinkedList<TIntObjectMap<TIntSet>> domain = new LinkedList<>();
    final LinkedList<TIntObjectMap<TIntList>> reverseDomain = new LinkedList<>();

    final MultipleKeyMap<Path> reachabilityCache = new MultipleKeyMap<>();
    final TIntObjectMap<Set<Path>> reversePathLookup = new TIntObjectHashMap<>();

    final int nReachLevel;
    final VertexMatching vertexMatching;

    private final MyGraph modifiedTargetGraph;

    public MReachCachedPruner(Settings settings,
                              MyGraph sourceGraph,
                              MyGraph targetGraph,
                              AbstractOccupation occupation,
                              VertexMatching vertexMatching,
                              int nReachLevel) {
        super(settings, sourceGraph, targetGraph, occupation);
        TIntObjectHashMap<TIntList> firstReverseDomain = new TIntObjectHashMap<>(targetGraph.vertexSet().size());
        targetGraph.vertexSet().forEach(x -> firstReverseDomain.put(x, new TIntLinkedList()));
        reverseDomain.offerFirst(firstReverseDomain);
        TIntObjectHashMap<TIntSet> firstDomain = new TIntObjectHashMap<>(sourceGraph.vertexSet().size());
        sourceGraph.vertexSet().forEach(x -> firstDomain.put(x, new TIntHashSet()));
        domain.offerFirst(firstDomain);
        this.vertexMatching = vertexMatching;
        initializeDomains();
        this.nReachLevel = nReachLevel;
        modifiedTargetGraph = new MyGraph(targetGraph);
    }

    public MReachCachedPruner(MReachCachedPruner copyOf) {
        super(copyOf.settings, copyOf.sourceGraph, copyOf.targetGraph, copyOf.occupation);
        domain.clear();
        for (TIntObjectMap<TIntSet> map : copyOf.domain) {
            TIntObjectMap<TIntSet> toAdd = new TIntObjectHashMap<>();
            map.forEachEntry((a, b) -> {
                toAdd.put(a, new TIntHashSet(b));
                return true;
            });
            domain.add(toAdd);
        }
        reverseDomain.clear();
        for (TIntObjectMap<TIntList> map : copyOf.reverseDomain) {
            TIntObjectMap<TIntList> toAdd = new TIntObjectHashMap<>();
            map.forEachEntry((a, b) -> {
                toAdd.put(a, new TIntLinkedList(b));
                return true;
            });
            reverseDomain.add(toAdd);
        }
        for (MultipleKeyMap<Path>.Entry entry : copyOf.reachabilityCache.entrySet()) {
            reachabilityCache.put(entry.getFirstKey(), entry.getSecondKey(), new Path(entry.getValue()));
        }
        copyOf.reversePathLookup.forEachEntry((key, value) -> {
            reversePathLookup.put(key, value.stream().map(Path::new).collect(Collectors.toSet()));
            return true;
        });
        this.nReachLevel = copyOf.nReachLevel;
        this.vertexMatching = copyOf.vertexMatching;
        modifiedTargetGraph = copyOf.modifiedTargetGraph;
    }

    public TIntSet getDomain(int sourceGraphVertex) {
        return getDomain(sourceGraphVertex, domain);
    }

    public TIntSet getDomain(int sourceGraphVertex, List<TIntObjectMap<TIntSet>> subList) {
        for (TIntObjectMap<TIntSet> map : subList) {
            if (map.containsKey(sourceGraphVertex)) {
                return map.get(sourceGraphVertex );
            }
        }
        throw new UnsupportedOperationException();
    }

    public TIntList getReverseDomain(int targetGraphVertex, List<TIntObjectMap<TIntList>> subList) {
        for (TIntObjectMap<TIntList> map : subList) {
            if (map.containsKey(targetGraphVertex)) {
                return map.get(targetGraphVertex);
            }
        }
        throw new UnsupportedOperationException();
    }

    private void initializeDomains() {
        UnmatchedDegreesFiltering subFiltering = new UnmatchedDegreesFiltering();
        targetGraph.vertexSet().forEach(x -> reverseDomain.peekFirst().put(x, new TIntLinkedList()));
        sourceGraph.vertexSet().forEach(source -> {
           TIntSet toPut = new TIntHashSet();
            targetGraph.vertexSet().forEach(target -> {
                if (subFiltering.filter(sourceGraph, targetGraph, source, target, occupation, vertexMatching)) {
                    toPut.add(target);
                    reverseDomain.peekFirst().get(target).add(source);
                }
            });
            domain.peekFirst().put(source, toPut);
        });
        targetGraph.vertexSet().forEach(x -> reversePathLookup.put(x, new HashSet<>()));
    }

    @Override
    public void afterReleaseVertex(int verticesPlaced, int released) {
        reverseDomain.pollFirst();
        domain.pollFirst();
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int released) {
        reverseDomain.pollFirst();
        domain.pollFirst();
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int targetVertex, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        if (!getDomain(verticesPlaced - 1, domain).contains(targetVertex)) {
            throw new DomainCheckerException(null);
        }
        TIntList singletonReverseDomain = new TIntLinkedList();
        TIntSet singletonDomain = new TIntHashSet();
        singletonReverseDomain.add(verticesPlaced - 1);
        singletonDomain.add(targetVertex);
        TIntObjectMap<TIntSet> newDomainLayer = new TIntObjectHashMap<>();
        newDomainLayer.put(verticesPlaced - 1, singletonDomain);
        domain.offerFirst(newDomainLayer);
        TIntObjectMap<TIntList> newReverseDomainLayer = new TIntObjectHashMap<>();
        newReverseDomainLayer.put(targetVertex, singletonReverseDomain);
        reverseDomain.offerFirst(newReverseDomainLayer);
        TIntSet singles = getSingles(verticesPlaced - 1, targetVertex, newDomainLayer);
        getDomain(verticesPlaced - 1, domain.subList(1, domain.size())).forEach(targetGraphVertex -> {
            if (targetGraphVertex != targetVertex) {
                TIntList old = new TIntLinkedList(getReverseDomain(targetGraphVertex, reverseDomain.subList(1, reverseDomain.size())));
                old.remove(verticesPlaced - 1);
                newReverseDomainLayer.put(targetGraphVertex, old);
            }
            return true;
        });
        reserveSingles(newDomainLayer, newReverseDomainLayer, singles);
        if (settings.getFiltering() instanceof MReachabilityFiltering || settings.getFiltering() instanceof NReachabilityFiltering) {
            Set<Integer> mustBePredecessors = Graphs.predecessorListOf(sourceGraph, verticesPlaced - 1).stream().filter(x -> x < vertexMatching.get().size()).map(x -> vertexMatching.get().get(x)).collect(Collectors.toUnmodifiableSet());
            Set<Integer> mustBeSuccessors = Graphs.successorListOf(sourceGraph, verticesPlaced - 1).stream().filter(x -> x < vertexMatching.get().size()).map(x -> vertexMatching.get().get(x)).collect(Collectors.toUnmodifiableSet());
            MReachabilityCheck(targetVertex, mustBePredecessors, false);
            MReachabilityCheck(targetVertex, mustBeSuccessors, true);
            if (settings.getFiltering() instanceof NReachabilityFiltering) {
                NReachabilityCheck(newDomainLayer, newReverseDomainLayer);
            }
        }
        if (isUnfruitfulCached(verticesPlaced)) {
            domain.pollFirst();
            reverseDomain.pollFirst();
            throw new DomainCheckerException(null);
        }
    }

    protected void NReachabilityCheck(TIntObjectMap<TIntSet> newDomainLayer, TIntObjectMap<TIntList> newReverseDomainLayer) {
        TIntSet keysToCheck = new TIntHashSet(newDomainLayer.keySet());
        int level = 0;
        while (!keysToCheck.isEmpty() || level >= nReachLevel) {
            int sourceGraphVertexToCheck = keysToCheck.iterator().next();
            keysToCheck.remove(sourceGraphVertexToCheck);
            TIntSet domain = getDomain(sourceGraphVertexToCheck);
            List<Integer> predecessors = Graphs.predecessorListOf(sourceGraph, sourceGraphVertexToCheck);
            List<Integer> successors = Graphs.successorListOf(sourceGraph, sourceGraphVertexToCheck);
            for (Integer predecessor : predecessors) {
                TIntSet predecessorDomain = getDomain(predecessor);
                new TIntHashSet(predecessorDomain).forEach(predecessorCandidate -> {
                    if (anyMatch(domain, x -> reachabilityCache.containsKey(predecessorCandidate, x))) {
                        return true;
                    }
                    Optional<Path> optionalPath = Util.filteredShortestPath(modifiedTargetGraph, occupation, new TIntHashSet(), new TIntHashSet(new int[] {predecessorCandidate}), domain);
                    if (optionalPath.isPresent()) {
                        reachabilityCache.put(predecessorCandidate, optionalPath.get().last(), optionalPath.get());
                        if (optionalPath.get().length() > 2) {
                            optionalPath.get().intermediate().forEach(x -> reversePathLookup.get(x).add(optionalPath.get()));
                        }
                    } else {
                        Graphs.neighborSetOf(sourceGraph, predecessor).forEach(keysToCheck::add);
                        removeFromDomain(newReverseDomainLayer, newDomainLayer, predecessor, predecessorCandidate);
                    }
                    return true;
                });
            }
            for (Integer successor : successors) {
                TIntSet successorDomain = getDomain(successor);
                new TIntHashSet(successorDomain).forEach(successorCandidate -> {
                    if (anyMatch(domain, x -> reachabilityCache.containsKey(x, successorCandidate))) {
                        return true;
                    }
                    Optional<Path> optionalPath = Util.filteredShortestPath(modifiedTargetGraph, occupation, new TIntHashSet(), domain, new TIntHashSet(new int[] {successorCandidate}));
                    if (optionalPath.isPresent()) {
                        reachabilityCache.put(optionalPath.get().first(), successorCandidate, optionalPath.get());
                        if (optionalPath.get().length() > 2) {
                            optionalPath.get().intermediate().forEach(x -> reversePathLookup.get(x).add(optionalPath.get()));
                        }
                    } else {
                        Graphs.neighborSetOf(sourceGraph, successor).forEach(keysToCheck::add);
                        removeFromDomain(newReverseDomainLayer, newDomainLayer, successor, successorCandidate);
                    }
                    return true;
                });
            }
            level++;
        }
    }

    private static boolean anyMatch(TIntCollection collection, Predicate<Integer> predicate) {
        return !collection.forEach(value -> !predicate.test(value));
    }

    private void MReachabilityCheck(int targetVertex, Set<Integer> neighbours, boolean successors) throws DomainCheckerException {
        for (int neighbour : neighbours) {
            int from = successors ? targetVertex : neighbour;
            int to = successors ? neighbour : targetVertex;
            if (!reachabilityCache.containsKey(from, to)) {
                Optional<Path> path = Util.filteredShortestPath(modifiedTargetGraph, occupation, new TIntHashSet(), from, to, false, -1);
                if (path.isEmpty()) {
                    domain.pollFirst();
                    reverseDomain.pollFirst();
                    throw new DomainCheckerException(null);
                } else {
                    reachabilityCache.put(from, to, path.get());
                    if (path.get().length() > 2) {
                        path.get().intermediate().forEach(x -> reversePathLookup.get(x).add(path.get()));
                    }
                }
            }
        }
    }


    private void reserveSingles(TIntObjectMap<TIntSet> newDomainLayer, TIntObjectMap<TIntList> newReverseDomainLayer, TIntSet singles) {
        while (singles.size() > 0) {
            int randomSingleVertex = singles.iterator().next();
            singles.remove(randomSingleVertex);
            if (getDomain(randomSingleVertex, domain).size() == 0) {
                continue;
            }
            TIntList reverseList = new TIntLinkedList();
            reverseList.add(randomSingleVertex);
            new TIntArrayList(getReverseDomain(getDomain(randomSingleVertex, domain).iterator().next(), reverseDomain)).forEach(sourceGraphVertex -> {
                if (sourceGraphVertex != randomSingleVertex) {
                    TIntSet old = new TIntHashSet(getDomain(sourceGraphVertex, domain));
                    old.remove(getDomain(randomSingleVertex, domain).iterator().next());
                    newDomainLayer.put(sourceGraphVertex, old);
                    if (old.size() == 1) {
                        singles.add(sourceGraphVertex);
                    }
                }
                return true;
            });
            newReverseDomainLayer.put(getDomain(randomSingleVertex, domain).iterator().next(), reverseList);
        }
    }

    private final Set<Path> justInvalidated = new HashSet<>();

    @Override
    public void afterOccupyEdge(int verticesPlaced, int targetVertex, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        afterOccupyEdgeWithoutCheck(verticesPlaced, targetVertex);
        if (isUnfruitfulCached(verticesPlaced)) {
            domain.pollFirst();
            reverseDomain.pollFirst();
            justInvalidated.forEach(path -> {
                reachabilityCache.put(path.first(), path.last(), path);
                if (path.length() > 2) {
                    path.intermediate().forEach(x -> reversePathLookup.get(x).add(path));
                }
            });
            throw new DomainCheckerException(null);
        }
    }


    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int targetVertex) {
        TIntObjectMap<TIntList> newReverseDomainLayer = new TIntObjectHashMap<>();
        TIntObjectMap<TIntSet> newDomainLayer = new TIntObjectHashMap<>();
        TIntList emptyReverseDomain = new TIntLinkedList();
        newReverseDomainLayer.put(targetVertex, emptyReverseDomain);
        domain.offerFirst(newDomainLayer);
        reverseDomain.offerFirst(newReverseDomainLayer);
        TIntSet singles = getSingles(-1, targetVertex, newDomainLayer);
        reserveSingles(newDomainLayer, newReverseDomainLayer, singles);
        if (settings.getFiltering() instanceof UnmatchedDegreesFiltering || settings.getFiltering() instanceof MReachabilityFiltering) {
            filterDegree(targetVertex, newReverseDomainLayer, newDomainLayer, true);
            filterDegree(targetVertex, newReverseDomainLayer, newDomainLayer, false);
        }
        new HashSet<>(reversePathLookup.get(targetVertex)).forEach(path -> {
            reversePathLookup.get(targetVertex).remove(path);
            reachabilityCache.remove(path.first(), path.last());
            justInvalidated.add(path);
        });
    }

    @NotNull
    private TIntSet getSingles(int allowed, int targetVertex, TIntObjectMap<TIntSet> newDomainLayer) {
        TIntSet singles = new TIntHashSet();
        getReverseDomain(targetVertex, reverseDomain.subList(1, reverseDomain.size())).forEach(sourceGraphVertex -> {
            if (sourceGraphVertex != allowed) {
                TIntSet old = new TIntHashSet(getDomain(sourceGraphVertex, domain.subList(1, domain.size())));
                old.remove(targetVertex);
                newDomainLayer.put(sourceGraphVertex, old);
                if (old.size() == 1) {
                    singles.add(sourceGraphVertex);
                }
            }
            return true;
        });
        return singles;
    }

    private void filterDegree(int targetVertex, TIntObjectMap<TIntList> newReverseDomainLayer, TIntObjectMap<TIntSet> newDomainLayer, boolean indegree) {
        List<Integer> neighbours = indegree ? Graphs.successorListOf(targetGraph, targetVertex) : Graphs.predecessorListOf(targetGraph, targetVertex);
        for (Integer successor : neighbours) {
            TIntList candidates = getReverseDomain(successor, reverseDomain.subList(1, reverseDomain.size()));
            new TIntHashSet(candidates).forEach(i -> {
                List<Integer> requirements = (indegree ? sourceGraph.incomingEdgesOf(i) : sourceGraph.outgoingEdgesOf(i)).stream().map(x -> Graphs.getOppositeVertex(sourceGraph, x, i)).filter(x -> x >= vertexMatching.get().size()).collect(Collectors.toList());
                int delivery = (int) (indegree ? targetGraph.incomingEdgesOf(successor) : targetGraph.outgoingEdgesOf(successor)).stream().map(x -> Graphs.getOppositeVertex(targetGraph, x, successor)).filter(x -> !occupation.isOccupiedRouting(x)).count();
                if (requirements.size() > delivery + (indegree ? 1 : 0)) { //the current vertex being occupied might just be attempting to reach this one.
                    removeFromDomain(newReverseDomainLayer, newDomainLayer, i, successor);
                }
                return true;
            });
        }
    }

    private void removeFromDomain(TIntObjectMap<TIntList> newReverseDomainLayer, TIntObjectMap<TIntSet> newDomainLayer, int sourceVertex, int targetVertex) {
        if (newDomainLayer.containsKey(sourceVertex)) {
            newDomainLayer.get(sourceVertex).remove(targetVertex);
        } else {
            TIntSet newDomain = new TIntHashSet(getDomain(sourceVertex, domain));
            newDomain.remove(targetVertex);
            newDomainLayer.put(sourceVertex, newDomain);
        }
        if (newReverseDomainLayer.containsKey(targetVertex)) {
            newReverseDomainLayer.get(targetVertex).remove(sourceVertex);
        } else {
            TIntList newReverseDomain = new TIntLinkedList(getReverseDomain(targetVertex, reverseDomain));
            newReverseDomain.remove(sourceVertex);
            newReverseDomainLayer.put(targetVertex, newReverseDomain);
        }
    }

    @Override
    public abstract boolean isUnfruitfulCached(int verticesPlaced);

    @Override
    public abstract Pruner copy();

    @Override
    public void close() {

    }

    @Override
    public void checkPartial(PartialMatchingProvider partialMatching) throws DomainCheckerException {

    }
}
