package com.charrey;

import com.charrey.exceptions.FullHomeomorphismFound;
import com.charrey.exceptions.NoSuchPairException;
import com.charrey.graph.Vertex;
import com.charrey.graph.RoutingVertexTable;
import com.charrey.matchResults.*;
import com.charrey.router.LockTable;
import com.charrey.router.Router;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.logging.Logger;

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
    private static final Logger LOGGER = Logger.getLogger("State");


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

//    private List<Vertex> newPlacement(Pair<Integer, Vertex> nextPair) {
//        List<Vertex> newPlacement = new ArrayList<>(placement);
//        if (nextPair.getFirst() == placement.size()) {
//            newPlacement.add(nextPair.getSecond());
//        } else {
//            newPlacement.set(nextPair.getFirst(), nextPair.getSecond());
//        }
//        return newPlacement;
//    }

//    private Set<Vertex> newOccupation(Pair<Integer, Vertex> nextPair) {
//        Set<Vertex> newOccupation = new HashSet<>(occupation);
//        if (nextPair.getFirst() < placement.size()) {
//            newOccupation.remove(placement.get(nextPair.getFirst()));
//        }
//        newOccupation.add(nextPair.getSecond());
//        return newOccupation;
//    }

    public MatchResult tryNext(Pair<Integer, Vertex> nextPair) {
        if (occupation.contains(nextPair.getSecond())) {
            LOGGER.finer(nextPair.getFirst() + "--" + nextPair.getSecond().getData() + " is occupied");
            return new OccupiedMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond());
        }
        LOGGER.finer("Success match " + nextPair.getFirst() + "--" + nextPair.getSecond().getData());
        return new SuccessMatchResult(vertices.get(nextPair.getFirst()), nextPair.getSecond());
    }

    public void update(SuccessMatchResult successMatchResult) {
        placement.add(successMatchResult.getTo());
        occupation.add(successMatchResult.getTo());
    }

    public Pair<Integer, Vertex> getNextPairExplore() throws FullHomeomorphismFound {
        if (nextTry.length <= placement.size()) {
            throw new FullHomeomorphismFound();
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


    public Pair<Integer, Vertex> getNextPairRetry(Pair<Integer, Vertex> previousPair) throws NoSuchPairException {
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
        throw new NoSuchPairException();
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
