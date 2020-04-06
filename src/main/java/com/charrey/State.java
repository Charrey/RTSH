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
public class State {


    private final List<AttributedVertex> placement;
    @SuppressWarnings("rawtypes")
    private final Map[] nextTry;
    private final Set<AttributedVertex> occupation;
    private final Router router;
    private final Graph<AttributedVertex, DefaultEdge> pattern;
    private final Graph<AttributedVertex, DefaultEdge> target;
    private final List<AttributedVertex> vertices;
    private final RoutingVertexTable routingVertexTable;
    private final LockTable lockTable;


    public State(Graph<AttributedVertex, DefaultEdge> pattern,
                 List<AttributedVertex> vertices,
                 Graph<AttributedVertex, DefaultEdge> target,
                 List<AttributedVertex> placement,
                 Map<AttributedVertex, AttributedVertex>[] nextTry,
                 Set<AttributedVertex> occupation,
                 Router router,
                 RoutingVertexTable routingVertexTable,
                 LockTable lockTable) {
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

    private List<AttributedVertex> newPlacement(Pair<Integer, AttributedVertex> nextPair) {
        List<AttributedVertex> newPlacement = new ArrayList<>(placement);
        if (nextPair.getFirst() == placement.size()) {
            newPlacement.add(nextPair.getSecond());
        } else {
            newPlacement.set(nextPair.getFirst(), nextPair.getSecond());
        }
        return newPlacement;
    }

    private Set<AttributedVertex> newOccupation(Pair<Integer, AttributedVertex> nextPair) {
        Set<AttributedVertex> newOccupation = new HashSet<>(occupation);
        if (nextPair.getFirst() < placement.size()) {
            newOccupation.remove(placement.get(nextPair.getFirst()));
        }
        newOccupation.add(nextPair.getSecond());
        return newOccupation;
    }

    public MatchResult tryNext(Pair<Integer, AttributedVertex> nextPair) {
        if (occupation.contains(nextPair.getSecond())) {
            return new OccupiedMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        }
        RoutingResult routing = router.route(nextPair.getSecond(),
                GraphUtil.neighboursOf(pattern, vertices.get(nextPair.getFirst()))
                        .stream()
                        .filter(x -> x.intData() < placement.size())
                        .map(v1 -> placement.get(v1.intData()))
                        .collect(Collectors.toSet()),
                target, routingVertexTable, lockTable);
        if (routing.hasFailed()) {
            return new RoutingFailedMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        } else if (routing.requiresExtraVertices()) {
            return new SuccessWithExtraVertices(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        }

        List<AttributedVertex> newPlacement = newPlacement(nextPair);
        Set<AttributedVertex> newOccupation = newOccupation(nextPair);
        return new SuccessMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond(), new State(pattern, vertices, target, newPlacement, nextTry, newOccupation, router, routingVertexTable, lockTable));
    }

    public Pair<Integer, AttributedVertex> getNextPairExplore() {
        if (nextTry[placement.size()].get(null) != null) {
            return new Pair<>(placement.size(), (AttributedVertex) nextTry[placement.size()].get(null));
        }
        for (int i = placement.size() - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (AttributedVertex) nextTry[i].get(placement.get(i)));
            }
        }
        throw new RuntimeException();
    }


    public Pair<Integer, AttributedVertex> getNextPairRetry(Pair<Integer, AttributedVertex> previousPair) {
        int from = previousPair.getFirst();
        AttributedVertex to = previousPair.getSecond();
        if (nextTry[from].get(to) != null) {
            return new Pair<>(from, (AttributedVertex) nextTry[from].get(to));
        }
        for (int i = from - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (AttributedVertex) nextTry[i].get(placement.get(i)));
            }
        }
        return null;
    }


    public List<AttributedVertex> getPlacement() {
        return placement;
    }

    public Cache.CacheEntry getParent() {
        throw new UnsupportedOperationException();
    }


    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "\tlocked:\t" + this.lockTable + "\n" +
                "}";
    }
}
