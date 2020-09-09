package com.charrey.matching;

import com.charrey.algorithms.AllDifferent;
import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Chain;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.PathIteratorFactory;
import com.charrey.pruning.serial.PartialMatching;
import com.charrey.settings.Settings;
import com.charrey.util.datastructures.MultipleKeyMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A class that saves which source graph edge is mapped to which target graph path, and provides methods to facilitate
 * such matchings.
 */
public class EdgeMatching implements Supplier<TIntObjectMap<Set<Path>>>, PartialMatchingProvider {

    private final VertexMatching vertexMatching;
    private final MyGraph source;
    @NotNull
    private final MyGraph targetGraph;
    private final boolean directed;
    private final Settings settings;
    private final long timeoutTime;

    private MultipleKeyMap<Deque<PathIterator>> pathfinders;
    private final GlobalOccupation occupation;

    private int[][] edges; //do not change
    private boolean[][] incoming; //only for directed graphs

    private ArrayList<LinkedList<Pair<Path, String>>> paths;

    private final UtilityData data;

    /**
     * Instantiates a new edgematching.
     *  @param vertexMatching the vertex matching class used in this homeomorphism finding session
     * @param data           the utility data class of this test case (for cached computations)
     * @param source         the source graph (new one)
     * @param target         the target graph
     * @param occupation     the global occupation which vertices have been used and which are available
     * @param timeoutTime
     */
    public EdgeMatching(VertexMatching vertexMatching, UtilityData data, MyGraph source, @NotNull MyGraph target, GlobalOccupation occupation, Settings settings, long timeoutTime) {
        this.vertexMatching = vertexMatching;
        this.source = source;
        this.data = data;
        this.targetGraph = target;
        this.directed = target.isDirected();
        initPathsEdges();
        initPathFinders();
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));
        EdgeMatching em = this;
        this.vertexMatching.setOnDeletion(em::synchronize);
        this.occupation = occupation;
        this.settings = settings;
        this.timeoutTime = timeoutTime;
    }

    private void initPathFinders() {
        pathfinders = new MultipleKeyMap<>();
        while (paths.size() < source.vertexSet().size()) {
            paths.add(new LinkedList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));
    }

    /**
     * Returns whether edges exists in the set of currently matched source graph vertices that have not been mapped
     * to target graph paths yet
     *
     * @return whether some edge can be matched
     */
    public boolean hasUnmatched() {
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        if (lastPlacedIndex == -1) {
            return false;
        }
        return paths.get(lastPlacedIndex).size() < edges[lastPlacedIndex].length;
    }

    /**
     * Attempts to replace the last matched edge-path mapping with a new one using the provided strategy.
     *
     * @return whether a new path has been successfully found
     */
    public boolean retry() {
        if (vertexMatching.getPlacement().isEmpty()) {
            return false;
        }
        List<Pair<Path, String>> pathList = paths.get(vertexMatching.getPlacement().size() - 1);
        if (pathList.isEmpty()) {
            return false;
        }
        for (int i = pathList.size() - 1; i >= 0; i--) {
            Path toRetry = pathList.get(i).getFirst();

            int tail = toRetry.first();
            int head = toRetry.last();
            assert pathfinders.containsKey(tail, head) && !pathfinders.get(tail, head).isEmpty();
            PathIterator pathfinder = pathfinders.get(tail, head).peekFirst();
            Path pathFound = pathfinder.next(); //was this pathfinder initiated with a crippled graph?
            if (pathFound != null) {

                assert pathFound.first() == tail : "Expected: " + tail + ", actual: " + pathFound.first();
                assert pathFound.last() == head : "Expected: " + head + ", actual: " + pathFound.last();
                Path toAdd = new Path(pathFound);
                pathList.set(pathList.size() - 1, new Pair<>(toAdd, pathfinder.debugInfo()));
                return true;
            } else {
                pathfinders.get(tail, head).removeFirst();
                if (pathfinders.get(tail, head).isEmpty()) {
                    pathfinders.remove(tail, head);
                }
                removeLastPath();
            }
        }
        return false;
    }

    private Pair<Integer, Integer> observingForChains;
    private Map<Path, Set<Chain>> satisfiedChains = new HashMap<>();

    /**
     * Adds a new mapping between a source graph edge and a target graph path (using the provided strategy) for a source
     * graph edge that is currently not yet matched.
     *
     * @return if a path has been found, returns that path. Otherwise, returns null.
     */
    @Nullable
    public Path placeNextUnmatched() {
        assert this.hasUnmatched();
        //get things
        int sourceGraphTo = vertexMatching.getPlacement().size() - 1;
        int sourceGraphFrom = edges[sourceGraphTo][paths.get(sourceGraphTo).size()];
        int from = vertexMatching.getPlacement().get(sourceGraphFrom);
        int to = vertexMatching.getPlacement().get(sourceGraphTo);
        if (directed && !incoming[sourceGraphTo][paths.get(sourceGraphTo).size()]) {
            //swap
            int temp = to;
            to = from;
            from = temp;
            temp = sourceGraphTo;
            sourceGraphTo = sourceGraphFrom;
            sourceGraphFrom = temp;
        }
        int tail;
        int head;
        if (directed) {
            tail = from;
            head = to;
        } else {
            tail = Math.min(from, to);
            head = Math.max(from, to);
        }
        //get pathIterator


        PathIterator iterator = PathIteratorFactory.get(targetGraph,
            data,
            tail,
            head,
            occupation,
            () -> vertexMatching.getPlacement().size(),
            settings,
            this,
            timeoutTime,
                getDirectConnectionsAlreadyUsed(tail, head));
        if (!pathfinders.containsKey(tail, head)) {
            pathfinders.put(tail, head, new LinkedList<>());
        }
        pathfinders.get(tail, head).addFirst(iterator);
        Path toReturn = iterator.next();
        if (toReturn != null) {
            toReturn = assertChainCompatible(sourceGraphTo, sourceGraphFrom, tail, head, iterator, toReturn);
            if (toReturn != null) {
                addPath(toReturn, iterator.debugInfo());
                return toReturn;
            } else {
                pathfinders.get(tail, head).removeFirst();
                if (pathfinders.get(tail, head).isEmpty()) {
                    pathfinders.remove(tail, head);
                }
                return null;
            }
        } else {
            pathfinders.get(tail, head).removeFirst();
            if (pathfinders.get(tail, head).isEmpty()) {
                pathfinders.remove(tail, head);
            }
            return null;
        }
    }

    @Nullable
    private Path assertChainCompatible(int sourceGraphTo, int sourceGraphFrom, int tail, int head, PathIterator iterator, Path toReturn) {
        if (settings.getContraction()) {
            if (!new Pair<>(sourceGraphFrom, sourceGraphTo).equals(observingForChains)) {
                observingForChains = new Pair<>(sourceGraphFrom, sourceGraphTo);
                satisfiedChains = new HashMap<>();
            }
            Set<Chain> chains = source.getChains(sourceGraphFrom, sourceGraphTo);
            Path finalToReturn1 = toReturn;
            Set<Chain> satisfied = chains.stream().filter(x -> x.compatible(finalToReturn1)).collect(Collectors.toSet());
            satisfiedChains.put(toReturn, satisfied);
            int stillToGo = matchesStillToGo(sourceGraphTo, sourceGraphFrom, tail, head);
            boolean chainOkay = checkChains(chains, stillToGo);
            while (!chainOkay) {
                toReturn = iterator.next();
                if (toReturn == null) {
                    break;
                }
                Path finalToReturn = toReturn;
                satisfied = chains.stream().filter(x -> x.compatible(finalToReturn)).collect(Collectors.toSet());
                satisfiedChains.put(toReturn, satisfied);
                chainOkay = checkChains(chains, stillToGo);
            }
        }
        return toReturn;
    }

    private int matchesStillToGo(int sourceGraphTo, int sourceGraphFrom, int tail, int head) {
        int initial = source.getAllEdges(sourceGraphFrom, sourceGraphTo).size();
        long done = paths.get(vertexMatching.getPlacement().size()-1)
                .stream()
                .filter(path -> List.of(path.getFirst().first(), path.getFirst().last()).equals(List.of(tail, head))).count();
        if (!source.isDirected()) {
            done -= 1;
        }
        return (int) (initial - done);
    }

    private boolean checkChains(Set<Chain> chains, int wildcards) {
        Map<Chain, Integer> chainMap = new HashMap<>();
        Map<Path, Integer> pathMap = new HashMap<>();
        int index = 0;
        for (Chain chain : chains) {
            chainMap.put(chain, index++);
        }
        index = 0;
        for (Path path : satisfiedChains.keySet()) {
            pathMap.put(path, index++);
        }
        List<TIntSet> res = new ArrayList<>(chainMap.size());
        for (int i = 0; i < chainMap.size(); i++) {
            res.add(new TIntHashSet());
        }
        for (Map.Entry<Path, Set<Chain>> entry : this.satisfiedChains.entrySet()) {
            for (Chain chain : entry.getValue()) {
                res.get(chainMap.get(chain)).add(pathMap.get(entry.getKey()));
            }
        }
        for (int i = 0; i < wildcards; i++) {
            int wildcard = index++;
            res.forEach(tIntSet -> tIntSet.add(wildcard));
        }
        return new AllDifferent().get(res);
    }


    private int getDirectConnectionsAlreadyUsed(int tail, int head) {
        int alreadyUsed = 0;
        if (!this.targetGraph.containsEdge(tail, head) || paths.get(vertexMatching.getPlacement().size()-1).isEmpty()) {
            return 0;
        }
        LinkedList<Pair<Path, String>> pathsPlaced = paths.get(vertexMatching.getPlacement().size()-1);
        ListIterator<Pair<Path, String>> listIterator = pathsPlaced.listIterator(pathsPlaced.size());
        do {
            Pair<Path, String> element = listIterator.previous();
            if (element.getFirst().isEqualTo(new Path(targetGraph, List.of(tail, head)))) {
                alreadyUsed++;
            } else if (element.getFirst().first() != tail || element.getFirst().last() != head) {
                break;
            }
        } while (listIterator.hasPrevious());
        return alreadyUsed;
    }

    private void initPathsEdges() {
        edges = new int[source.vertexSet().size()][];
        paths = new ArrayList<>(source.vertexSet().size());
        incoming = new boolean[source.vertexSet().size()][];
        for (int i = 0; i < source.vertexSet().size(); i++) {
            int tempi = i;
            if (!directed) {
                edges[i] = source.edgesOf(tempi).stream().map(x -> Graphs.getOppositeVertex(source, x, tempi)).filter(x -> x <= tempi).mapToInt(x -> x).toArray();
                //edges[i] = Graphs.neighborSetOf(source, tempi).stream().filter(x -> x <= tempi).mapToInt(x -> x).toArray();
            } else {
                List<Integer> incomingEdges = IntStream.range(0, tempi).boxed()
                        .filter(x -> source.getEdge(x, tempi) != null)
                        .collect(Collectors.toList());
                new ArrayList<>(incomingEdges).forEach(predecessor -> {
                    for (int j = 0; j < source.getAllEdges(predecessor, tempi).size() - 1; j++) {
                        incomingEdges.add(predecessor);
                    }
                });
                List<Integer> outgoingEdges = IntStream.range(0, tempi + 1 ).boxed()
                        .filter(x -> source.getEdge(tempi, x) != null)
                        .collect(Collectors.toList());
                new ArrayList<>(outgoingEdges).forEach(successor -> {
                    for (int j = 0; j < source.getAllEdges(tempi, successor).size() - 1; j++) {
                        outgoingEdges.add(successor);
                    }
                });
                incoming[i] = new boolean[incomingEdges.size() + outgoingEdges.size()];
                edges[i] = new int[incomingEdges.size() + outgoingEdges.size()];
                for (int j = 0; j < incomingEdges.size(); j++) {
                    incoming[i][j] = true;
                    edges[i][j] = incomingEdges.get(j);
                }
                for (int j = 0; j < outgoingEdges.size(); j++) {
                    edges[i][j + incomingEdges.size()] = outgoingEdges.get(j);
                }
            }
            paths.add(new LinkedList<>());
        }
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));

    }

    private void addPath(@NotNull Path found, String debugInfo) {
        assert !found.isEmpty();
        assert directed || found.last() >= found.first();
        int lastPlacedIndex = vertexMatching.getPlacement().size() - 1;
        Path added = new Path(found);
        paths.get(lastPlacedIndex).add(new Pair<>(added, debugInfo));
    }

    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EdgeMatching " + (directed ? "directed " : "") + "{\n");
        for (List<Pair<Path, String>> pathAddition : paths) {
            sb.append("\t").append(pathAddition).append("\n");
        }
        sb.append("}\n");
        assert paths.stream().allMatch(x -> x.stream().noneMatch(y -> y.getFirst().isEmpty()));
        return sb.toString();
    }

    private void synchronize(int vertex) {
        assert !vertexMatching.getPlacement().contains(vertex);
        paths.get(vertexMatching.getPlacement().size()).clear();
        observingForChains = null;
    }

    /**
     * Returns all paths in the target graph that are part of the current matching
     *
     * @return all matched target graph paths
     */
    @NotNull
    public Set<Path> allPaths() {
        Set<Path> res = new HashSet<>();
        for (LinkedList<Pair<Path, String>> pathList : paths) {
            pathList.forEach(x -> res.add(x.getFirst()));
        }
        return Collections.unmodifiableSet(res);
    }

    private void removeLastPath() {
        List<Pair<Path, String>> pathList = this.paths.get(this.vertexMatching.getPlacement().size() - 1);
        Path removed = pathList.remove(pathList.size() - 1).getFirst();
        assert directed || removed.last() >= removed.first();
    }


    @Override
    public PartialMatching getPartialMatching() {
        return vertexMatching.getPartialMatching();
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public TIntObjectMap<Set<Path>> get() {
        TIntObjectMap<Set<Path>> toReturn = new TIntObjectHashMap<>();
        for (int i = 0; i < paths.size(); i++) {
            toReturn.put(i, new HashSet<>(paths.get(i).stream().map(x -> new Path(x.getFirst())).collect(Collectors.toSet())));
        }
        return toReturn;
    }
}
