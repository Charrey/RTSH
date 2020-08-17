package com.charrey.graph;

import com.charrey.util.GraphUtil;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

public class Contractor {


    MyGraph contractDirected(MyGraph input) {
        final MyGraph res = GraphUtil.copy(input, null).graph;
        Set<Integer> toContract = res.vertexSet().stream().filter(x -> res.inDegreeOf(x) == 1 && res.outDegreeOf(x) == 1).collect(Collectors.toSet());
        Map<MyEdge, List<Map<String, Set<String>>>> chains = new HashMap<>();
        while (!toContract.isEmpty()) {
            int vertexObserving = toContract.iterator().next();
            assert res.containsVertex(vertexObserving);
            toContract.remove(vertexObserving);
            if (suitableForContraction(res, vertexObserving)) {
                int successor = Graphs.successorListOf(res, vertexObserving).get(0);
                int predecessor = Graphs.predecessorListOf(res, vertexObserving).get(0);
                Map<String, Set<String>> attributes = res.getAttributes(vertexObserving);
                contractSingleVertex(res, vertexObserving);
                List<Map<String, Set<String>>> chain = new LinkedList<>();
                chain.add(attributes);
                while (suitableForContraction(res, successor)) {
                    int newSuccessor = Graphs.successorListOf(res, successor).get(0);
                    toContract.remove(successor);
                    attributes = res.getAttributes(successor);
                    contractSingleVertex(res, successor);
                    chain.add(attributes);
                    successor = newSuccessor;
                }
                while (suitableForContraction(res, predecessor)) {
                    int newPredecessor = Graphs.predecessorListOf(res, predecessor).get(0);
                    toContract.remove(predecessor);
                    attributes = res.getAttributes(predecessor);
                    contractSingleVertex(res, predecessor);
                    chain.add(0, attributes);
                    predecessor = newPredecessor;
                }
                chains.put(res.getEdge(predecessor, successor), Collections.unmodifiableList(chain));
            }
        }
        res.setChains(Collections.unmodifiableMap(chains));
        return GraphUtil.repairVertices(res);
    }

    public MyGraph contractUndirected(MyGraph graph) {
        throw new UnsupportedOperationException();
    }

    private void contractSingleVertex(MyGraph graph, int vertex) {
        assert suitableForContraction(graph, vertex);
        int predecessor = Graphs.predecessorListOf(graph, vertex).get(0);
        int successor = Graphs.successorListOf(graph, vertex).get(0);
        if (successor == vertex || predecessor==vertex) {
            throw new IllegalStateException("Cannot contract self-loop");
        }
        graph.removeVertex(vertex);
        graph.addEdge(predecessor, successor);
    }

    private static boolean suitableForContraction(MyGraph graph, int vertex) {
        assert graph.containsVertex(vertex);
        List<Integer> predecessors = Graphs.predecessorListOf(graph, vertex);
        if (predecessors.size() != 1) {
            return false;
        }
        List<Integer> successors = Graphs.successorListOf(graph, vertex);
        return successors.size() == 1 && !predecessors.get(0).equals(vertex) ;
    }
}
