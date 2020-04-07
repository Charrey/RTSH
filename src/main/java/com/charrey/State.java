package com.charrey;

import com.charrey.exceptions.FullyExploredException;
import com.charrey.graph.Vertex;
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


    private final List<Vertex> placement;
    @SuppressWarnings("rawtypes")
    private final Map[] nextTry;
    private final Set<Vertex> occupation;
    private final Router router;
    private final Graph<Vertex, DefaultEdge> pattern;
    private final Graph<Vertex, DefaultEdge> target;
    private final List<Vertex> vertices;
    private final RoutingVertexTable routingVertexTable;
    private final LockTable lockTable;


    public State(Graph<Vertex, DefaultEdge> pattern,
                 List<Vertex> vertices,
                 Graph<Vertex, DefaultEdge> target,
                 List<Vertex> placement,
                 Map<Vertex, Vertex>[] nextTry,
                 Set<Vertex> occupation,
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

    private List<Vertex> newPlacement(Pair<Integer, Vertex> nextPair) {
        List<Vertex> newPlacement = new ArrayList<>(placement);
        if (nextPair.getFirst() == placement.size()) {
            newPlacement.add(nextPair.getSecond());
        } else {
            newPlacement.set(nextPair.getFirst(), nextPair.getSecond());
        }
        return newPlacement;
    }

    private Set<Vertex> newOccupation(Pair<Integer, Vertex> nextPair) {
        Set<Vertex> newOccupation = new HashSet<>(occupation);
        if (nextPair.getFirst() < placement.size()) {
            newOccupation.remove(placement.get(nextPair.getFirst()));
        }
        newOccupation.add(nextPair.getSecond());
        return newOccupation;
    }

    public MatchResult tryNext(Pair<Integer, Vertex> nextPair) {
        if (occupation.contains(nextPair.getSecond())) {
            return new OccupiedMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        }
        List<Vertex> newPlacement = newPlacement(nextPair);
        Set<Vertex> newOccupation = newOccupation(nextPair);
        return new SuccessMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond(), new State(pattern, vertices, target, newPlacement, nextTry, newOccupation, router, routingVertexTable, lockTable));
    }

    public Pair<Integer, Vertex> getNextPairExplore() throws FullyExploredException {
        if (nextTry.length <= placement.size()) {
            throw new FullyExploredException();
        }
        if (nextTry[placement.size()].get(null) != null) {
            return new Pair<>(placement.size(), (Vertex) nextTry[placement.size()].get(null));
        }
        for (int i = placement.size() - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (Vertex) nextTry[i].get(placement.get(i)));
            }
        }
        throw new RuntimeException();
    }


    public Pair<Integer, Vertex> getNextPairRetry(Pair<Integer, Vertex> previousPair) {
        int from = previousPair.getFirst();
        Vertex to = previousPair.getSecond();
        if (nextTry[from].get(to) != null) {
            return new Pair<>(from, (Vertex) nextTry[from].get(to));
        }
        for (int i = from - 1; i >=0; i--) {
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (Vertex) nextTry[i].get(placement.get(i)));
            }
        }
        return null;
    }


    public List<Vertex> getPlacement() {
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
