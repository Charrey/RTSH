package com.charrey.matching;

import com.charrey.example.GraphGenerator;
import com.charrey.exceptions.NoSuchPairException;
import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import com.charrey.matching.matchResults.vertex.VertexMatchResult;
import com.charrey.matching.matchResults.vertex.OccupiedMatchResult;
import com.charrey.matching.matchResults.vertex.SuccessMatchResult;
import com.charrey.router.LockTable;
import com.charrey.util.UtilityData;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.logging.Logger;

public class VertexMatching implements VertexBlocker {


    private final List<Vertex> placement = new LinkedList<>();
    @SuppressWarnings("rawtypes")
    private final Map[] nextTry;
    private final Set<Vertex> occupation = new HashSet<>();
    private final List<Vertex> order;
    private final RoutingVertexTable routingVertexTable;
    private final LockTable lockTable = new LockTable();
    private static final Logger LOGGER = Logger.getLogger("State");


    public VertexMatching(UtilityData data, GraphGenerator.GraphGeneration pattern,
                          GraphGenerator.GraphGeneration target) {
        this.nextTry = data.getToTryNext();
        this.order = data.getOrder();
        this.routingVertexTable = target.getRoutingTable();
    }

    public int matched() {
        return placement.size();
    }

    public boolean hasNext() {
        if (placement.size() >= nextTry.length) {
            return false;
        }
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


    public VertexMatchResult tryNext(Pair<Integer, Vertex> nextPair) {
        if (occupation.contains(nextPair.getSecond())) {
            LOGGER.finer(nextPair.getFirst() + "--" + nextPair.getSecond().getData() + " is occupied");
            return OccupiedMatchResult.instance;
        }
        LOGGER.finer("Success match " + nextPair.getFirst() + "--" + nextPair.getSecond().getData());
        return new SuccessMatchResult(order.get(nextPair.getFirst()), nextPair.getSecond());
    }

    public void update(SuccessMatchResult successMatchResult) {
        placement.add(successMatchResult.getTo());
        occupation.add(successMatchResult.getTo());
    }

    public Pair<Integer, Vertex> explore() {
        if (nextTry.length <= placement.size()) {
            return null;
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


    public Pair<Integer, Vertex> retry(Pair<Integer, Vertex> previousPair) throws NoSuchPairException {
        int from = previousPair.getFirst();
        Vertex to = previousPair.getSecond();
        if (nextTry[from].get(to) != null) {
            return new Pair<>(from, (Vertex) nextTry[from].get(to));
        }
        for (int i = from - 1; i >=0; i--) {
            assert occupation.remove(placement.get(i + 1));
            if (nextTry[i].get(placement.get(i)) != null) {
                return new Pair<>(i, (Vertex) nextTry[i].get(placement.get(i)));
            }
        }
        throw new NoSuchPairException();
    }


    public List<Vertex> getPlacement() {
        return placement;
    }


    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "\tlocked:\t" + this.lockTable + "\n" +
                "}";
    }


    public int unmatched() {
        return order.size() - placement.size();
    }

    public List<Vertex> getOrder() {
        return order;
    }

    @Override
    public boolean blocks(Vertex v) {
        return occupation.contains(v);
    }
}
