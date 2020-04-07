package com.charrey;

import com.charrey.example.GraphGenerator;
import com.charrey.exceptions.FullHomeomorphismFound;
import com.charrey.exceptions.NoSuchPairException;
import com.charrey.graph.RoutingVertexTable;
import com.charrey.graph.Vertex;
import com.charrey.matchResults.MatchResult;
import com.charrey.matchResults.OccupiedMatchResult;
import com.charrey.matchResults.SuccessMatchResult;
import com.charrey.router.LockTable;
import com.charrey.util.UtilityData;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;
import java.util.logging.Logger;

public class State {


    private final List<Vertex> placement = new LinkedList<>();
    @SuppressWarnings("rawtypes")
    private final Map[] nextTry;
    private final Set<Vertex> occupation = new HashSet<>();
    private final List<Vertex> order;
    private final RoutingVertexTable routingVertexTable;
    private final LockTable lockTable = new LockTable();
    private static final Logger LOGGER = Logger.getLogger("State");


    public State(GraphGenerator.GraphGeneration pattern,
                 GraphGenerator.GraphGeneration target) {
        UtilityData utilityData = new UtilityData(pattern.getGraph(), target.getGraph());
        this.nextTry = utilityData.getToTryNext();
        this.order = utilityData.getOrder();
        this.routingVertexTable = target.getRoutingTable();
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


    public MatchResult tryNext(Pair<Integer, Vertex> nextPair) {
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

    public Pair<Integer, Vertex> explore() throws FullHomeomorphismFound {
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


    public Pair<Integer, Vertex> retry(Pair<Integer, Vertex> previousPair) throws NoSuchPairException {
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


    @Override
    public String toString() {
        return "State {\n" + "\tplacement:\t" + placement + "\n" +
                "\tlocked:\t" + this.lockTable + "\n" +
                "}";
    }


}
