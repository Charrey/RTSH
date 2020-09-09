package com.charrey.pruning.cached;

import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.AbstractOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;
import com.charrey.util.Util;
import com.charrey.util.datastructures.MultipleKeyMap;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

public abstract class MReachCachedPruner extends Pruner {

    final LinkedList<TIntObjectMap<TIntSet>> domain = new LinkedList<>();
    final LinkedList<TIntObjectMap<TIntList>> reverseDomain = new LinkedList<>();

    final MultipleKeyMap<Path> reachabilityCache = new MultipleKeyMap<>();
    final TIntObjectMap<Set<Path>> reversePathLookup = new TIntObjectHashMap<>();

    final int nReachLevel;
    final VertexMatching vertexMatching;

    public MReachCachedPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, AbstractOccupation occupation, VertexMatching vertexMatching, int nReachLevel) {
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
        //assert !isUnfruitfulCached(verticesPlaced);
    }

    @Override
    public void afterReleaseEdge(int verticesPlaced, int released) {
        reverseDomain.pollFirst();
        domain.pollFirst();
        //assert !isUnfruitfulCached(verticesPlaced);
    }

    @Override
    public void beforeOccupyVertex(int verticesPlaced, int targetVertex, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        //assertDomainReverseDomainConsistent();
        //assertOnlyReducesDomains();
        //assert !isUnfruitfulCached(verticesPlaced);

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
        //assertDomainReverseDomainConsistent();
        Set<Integer> mustBePredecessors = Graphs.predecessorListOf(sourceGraph, verticesPlaced - 1).stream().filter(x -> x < vertexMatching.get().size()).map(x -> vertexMatching.get().get(x)).collect(Collectors.toUnmodifiableSet());
        Set<Integer> mustBeSuccessors = Graphs.successorListOf(sourceGraph, verticesPlaced - 1).stream().filter(x -> x < vertexMatching.get().size()).map(x -> vertexMatching.get().get(x)).collect(Collectors.toUnmodifiableSet());

        MReachabilityCheck(targetVertex, mustBePredecessors, false);
        //assertDomainReverseDomainConsistent();
        MReachabilityCheck(targetVertex, mustBeSuccessors, true);
        //assertDomainReverseDomainConsistent();
        //assertOnlyReducesDomains();
        if (isUnfruitfulCached(verticesPlaced)) {
            domain.pollFirst();
            reverseDomain.pollFirst();
            throw new DomainCheckerException(null);
        }
    }



    private void MReachabilityCheck(int targetVertex, Set<Integer> neighbours, boolean successors) throws DomainCheckerException {

        for (int neighbour : neighbours) {
            int from = successors ? targetVertex : neighbour;
            int to = successors ? neighbour : targetVertex;
            if (reachabilityCache.get(from, to) == null) {
                Optional<Path> path = Util.filteredShortestPath(targetGraph, occupation, new TIntHashSet(), from, to, false, -1);
                if (path.isEmpty()) {
                    domain.pollFirst();
                    reverseDomain.pollFirst();
                    throw new DomainCheckerException(null);
                } else {
                    reachabilityCache.put(from, to, path.get());
                    path.get().intermediate().forEach(x -> reversePathLookup.get(x).add(path.get()));
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


    @Override
    public void afterOccupyEdge(int verticesPlaced, int targetVertex, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        afterOccupyEdgeWithoutCheck(verticesPlaced, targetVertex);
        if (isUnfruitfulCached(verticesPlaced)) {
            domain.pollFirst();
            reverseDomain.pollFirst();
            throw new DomainCheckerException(null);
        }
    }

//    private void assertDomainReverseDomainConsistent() {
//        for (Integer vertex : sourceGraph.vertexSet()) {
//            getDomain(vertex, domain).forEach(value -> {
//                assert getReverseDomain(value, reverseDomain).contains(vertex) : "Domain of " + vertex + " contains " + value + " but reverse domain of " + value + " does not contain " + vertex;
//                return true;
//            });
//        }
//
//        for (Integer vertex : targetGraph.vertexSet()) {
//            getReverseDomain(vertex, reverseDomain).forEach(value -> {
//                assert getDomain(value, domain).contains(vertex) : "Reverse domain of " + vertex + " contains " + value + " but domain of " + value + " does not contain " + vertex ;
//                return true;
//            });
//        }
//    }

//    private void assertOnlyReducesDomains() {
//        TIntObjectMap<TIntSet> previous = new TIntObjectHashMap<>();
//        domain.getLast().forEachEntry((sourceGraphVertex, domain) -> {
//            previous.put(sourceGraphVertex, new TIntHashSet(domain));
//            return true;
//        });
//        Iterator<TIntObjectMap<TIntSet>> iterator = domain.descendingIterator();
//        while (iterator.hasNext()) {
//            TIntObjectMap<TIntSet> next = iterator.next();
//            next.forEachEntry((i, tIntSet) -> {
//                assert previous.get(i).containsAll(tIntSet);
//                previous.put(i, new TIntHashSet(tIntSet));
//                return true;
//            });
//        }
//        TIntObjectMap<TIntList> previousReverse = new TIntObjectHashMap<>();
//        reverseDomain.getLast().forEachEntry((targetGraphVertex, reversedomain) -> {
//            previousReverse.put(targetGraphVertex, new TIntLinkedList(reversedomain));
//            return true;
//        });
//        Iterator<TIntObjectMap<TIntList>> iteratorReverse = reverseDomain.descendingIterator();
//        while (iteratorReverse.hasNext()) {
//            TIntObjectMap<TIntList> next = iteratorReverse.next();
//            next.forEachEntry((i, tIntList) -> {
//                assert containsAll(previousReverse.get(i), tIntList);
//                previousReverse.put(i, new TIntLinkedList(tIntList));
//                return true;
//            });
//        }
//    }

    private boolean containsAll(TIntList container, TIntList contained) {
        if (container.isEmpty() && !contained.isEmpty()) {
            return false;
        }
        for (TIntIterator it = contained.iterator(); it.hasNext();) {
            int i = it.next();
            if (!(container.contains(i)))
                return false;
        }
        return true;
    }

    @Override
    public void afterOccupyEdgeWithoutCheck(int verticesPlaced, int targetVertex) {
        //assertDomainReverseDomainConsistent();
        //assertOnlyReducesDomains();
        TIntObjectMap<TIntList> newReverseDomainLayer = new TIntObjectHashMap<>();
        TIntObjectMap<TIntSet> newDomainLayer = new TIntObjectHashMap<>();
        TIntList emptyReverseDomain = new TIntLinkedList();
        newReverseDomainLayer.put(targetVertex, emptyReverseDomain);
        domain.offerFirst(newDomainLayer);
        reverseDomain.offerFirst(newReverseDomainLayer);
        TIntSet singles = getSingles(-1, targetVertex, newDomainLayer);
        //assertDomainReverseDomainConsistent();
        reserveSingles(newDomainLayer, newReverseDomainLayer, singles);
        //assertDomainReverseDomainConsistent();
        //assertOnlyReducesDomains();
        filterDegree(targetVertex, newReverseDomainLayer, newDomainLayer, true);
        filterDegree(targetVertex, newReverseDomainLayer, newDomainLayer, false);
        //assertDomainReverseDomainConsistent();
        //assertOnlyReducesDomains();
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
                List<Integer> requirements = (indegree ? sourceGraph.incomingEdgesOf(i) : sourceGraph.outgoingEdgesOf(i))
                        .stream()
                        .map(x -> Graphs.getOppositeVertex(sourceGraph, x, i))
                        .filter(x -> x >= vertexMatching.get().size()).collect(Collectors.toList());
                int delivery = (int) (indegree ? targetGraph.incomingEdgesOf(successor) : targetGraph.outgoingEdgesOf(successor))
                        .stream().map(x -> Graphs.getOppositeVertex(targetGraph, x, successor))
                        .filter(x -> !occupation.isOccupiedRouting(x)).count();
                if (requirements.size() > delivery + (indegree ? 1 : 0)) { //the current vertex being occupied might just be attempting to reach this one.
                    if (newDomainLayer.containsKey(i)) {
                        newDomainLayer.get(i).remove(successor);
                        //assertOnlyReducesDomains();
                    } else {
                        TIntSet newDomain = new TIntHashSet(getDomain(i, domain));
                        newDomain.remove(successor);
                        newDomainLayer.put(i, newDomain);
                        //assertOnlyReducesDomains();
                    }
                    if (newReverseDomainLayer.containsKey(successor)) {
                        newReverseDomainLayer.get(successor).remove(i);
                        //assertDomainReverseDomainConsistent();
                        //assertOnlyReducesDomains();
                    } else {
                        TIntList newReverseDomain = new TIntLinkedList(getReverseDomain(successor, reverseDomain.subList(1, reverseDomain.size())));
                        newReverseDomain.remove(i);
                        newReverseDomainLayer.put(successor, newReverseDomain);
                        //assertDomainReverseDomainConsistent();
                        //assertOnlyReducesDomains();
                    }
                }
                return true;
            });
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
