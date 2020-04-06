package com.charrey;

import com.charrey.graph.AttributedVertex;
import com.charrey.graph.RoutingVertexTable;
import com.charrey.matchResults.*;
import com.charrey.router.LockTable;
import com.charrey.router.Router;
import com.charrey.router.RoutingResult;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class State<V2 extends Comparable<V2>> {


    private final List<V2> placement;
    private final Map[] nextTry;
    private final Set<V2> occupation;
    private final Router<V2> router;
    private final Graph<AttributedVertex<Integer>, DefaultEdge> pattern;
    private final Graph<V2, DefaultEdge> target;
    private final List<AttributedVertex<Integer>> vertices;
    private final RoutingVertexTable<V2> routingVertexTable;
    private final LockTable<V2> lockTable;


    public State(Graph<AttributedVertex<Integer>, DefaultEdge> pattern,
                 List<AttributedVertex<Integer>> vertices,
                 Graph<V2, DefaultEdge> target,
                 List<V2> placement,
                 Map<V2, V2>[] nextTry,
                 Set<V2> occupation,
                 Router<V2> router,
                 RoutingVertexTable<V2> routingVertexTable,
                 LockTable<V2> lockTable) {
        this.placement = placement;
        this.nextTry = Arrays.stream(nextTry).map(Collections::unmodifiableMap).toArray(Map[]::new);
        this.occupation = occupation;
        this.router = router;
        this.pattern = pattern;
        this.target = target;
        this.vertices = vertices;
        this.routingVertexTable = routingVertexTable;
        this.lockTable = lockTable;
    }

    public int numberOfMatchedVertices() {
        return placement.size();
    }

    public boolean hasNext() {
        if (nextTry[placement.size()].get(null) != null) {
            return true;
        }
        for (int i = placement.size() - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return true;
            }
        }
        return false;
    }

    private List<V2> newPlacement(Pair<Integer, V2> nextPair) {
        List<V2> newPlacement = new ArrayList<>(placement);
        if (nextPair.getFirst() == placement.size()) {
            newPlacement.add(nextPair.getSecond());
        } else {
            newPlacement.set(nextPair.getFirst(), nextPair.getSecond());
        }
        return newPlacement;
    }

    private Set<V2> newOccupation(Pair<Integer, V2> nextPair) {
        Set<V2> newOccupation = new HashSet<>(occupation);
        if (nextPair.getFirst() < placement.size()) {
            newOccupation.remove(placement.get(nextPair.getFirst()));
        }
        newOccupation.add(nextPair.getSecond());
        return newOccupation;
    }

    public MatchResult<AttributedVertex<Integer>, V2> tryNext(Pair<Integer, V2> nextPair) {
        if (occupation.contains(nextPair.getSecond())) {
            return new OccupiedMatchResult<>(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        }
        RoutingResult<V2> routing = router.route(nextPair.getSecond(),
                GraphUtil.neighboursOf(pattern,
                        vertices.get(nextPair.getFirst())).stream().filter(x -> x.id < placement.size()).map(v1 -> placement.get(v1.id)).collect(Collectors.toSet()),
                target, routingVertexTable, lockTable);
        if (routing.hasFailed()) {
            return new RoutingFailedMatchResult<>(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        } else if (routing.requiresExtraVertices()) {
            return new SuccessWithExtraVertices<>(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        }

        List<V2> newPlacement = newPlacement(nextPair);
        Set<V2> newOccupation = newOccupation(nextPair);
        return new SuccessMatchResult<>(vertices.get(nextPair.getFirst()), nextPair.getSecond(), new State<V2>(pattern, vertices, target, newPlacement, nextTry, newOccupation, router, routingVertexTable, lockTable));
    }

    public Pair<Integer, V2> getNextPairExplore() {
        if (nextTry[placement.size()].get(null) != null) {
            return new Pair<>(placement.size(), (V2) nextTry[placement.size()].get(null));
        }
        for (int i = placement.size() - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (V2) nextTry[i].get(placement.get(i)));
            }
        }
        throw new RuntimeException();
    }


    public Pair<Integer, V2> getNextPairRetry(Pair<Integer, AttributedVertex<Character>> previousPair) {
        int from = previousPair.getFirst();
        AttributedVertex<Character> to = previousPair.getSecond();
        if (nextTry[from].get(to) != null) {
            return new Pair<>(from, (V2) nextTry[from].get(to));
        }
        for (int i = from - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (V2) nextTry[i].get(placement.get(i)));
            }
        }
        return null;
    }


    public List<V2> getPlacement() {
        return placement;
    }

    public Cache.CacheEntry getParent() {
        throw new UnsupportedOperationException();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("State {\n");
        sb.append("\tplacement:\t").append(placement).append("\n");
        sb.append("\tlocked:\t").append(this.lockTable).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
