package com.charrey.graph;

import com.charrey.algorithms.vertexordering.Mapping;
import com.charrey.util.GraphUtil;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

public class Contractor {


    ContractResult contractDirected(MyGraph input) {
        final MyGraph res = GraphUtil.copy(input, null).graph;
        Set<Integer> toContract = res.vertexSet().stream().filter(x -> res.inDegreeOf(x) == 1 && res.outDegreeOf(x) == 1).collect(Collectors.toSet());
        Map<MyEdge, Chain> chains = new HashMap<>();

        Map<Chain, LinkedList<Integer>> origins = new HashMap<>();
        while (!toContract.isEmpty()) {
            int vertexObserving = toContract.iterator().next();
            assert res.containsVertex(vertexObserving);
            toContract.remove(vertexObserving);
            if (suitableForContractionDirected(res, vertexObserving)) {
                int successor = Graphs.successorListOf(res, vertexObserving).get(0);
                int predecessor = Graphs.predecessorListOf(res, vertexObserving).get(0);
                Set<String> labels = res.getAttributes(vertexObserving).getOrDefault("label", Collections.emptySet());
                MyEdge contracted = contractSingleVertexDirected(res, vertexObserving);
                Chain chain = new Chain();
                origins.put(chain, new LinkedList<>(List.of(vertexObserving)));
                chain.append(labels);
                while (suitableForContractionDirected(res, successor)) {
                    int newSuccessor = Graphs.successorListOf(res, successor).get(0);
                    toContract.remove(successor);
                    labels = res.getAttributes(successor).getOrDefault("label", Collections.emptySet());
                    contracted = contractSingleVertexDirected(res, successor);
                    origins.get(chain).addLast(successor);
                    chain.append(labels);
                    successor = newSuccessor;
                }
                while (suitableForContractionDirected(res, predecessor)) {
                    int newPredecessor = Graphs.predecessorListOf(res, predecessor).get(0);
                    toContract.remove(predecessor);
                    labels = res.getAttributes(predecessor).getOrDefault("label", Collections.emptySet());
                    contracted = contractSingleVertexDirected(res, predecessor);
                    origins.get(chain).addFirst(predecessor);
                    chain.prepend(labels);
                    predecessor = newPredecessor;
                }
                chains.put(contracted, chain.lock());
            }
        }
        res.setChains(Collections.unmodifiableMap(chains));
        Mapping result = GraphUtil.repairVertices(res);
        return new ContractResult(result.graph, result.newToOld, origins);
    }

    public ContractResult contractUndirected(MyGraph input) {
        final MyGraph res = GraphUtil.copy(input, null).graph;
        Set<Integer> toContract = res.vertexSet().stream().filter(x -> res.degreeOf(x) == 2).collect(Collectors.toSet());
        Map<MyEdge, Chain> chains = new HashMap<>();
        Map<Chain, LinkedList<Integer>> origins = new HashMap<>();
        while (!toContract.isEmpty()) {
            int vertexObserving = toContract.iterator().next();
            assert res.containsVertex(vertexObserving);
            toContract.remove(vertexObserving);
            if (res.degreeOf(vertexObserving) == 2 && !res.containsEdge(vertexObserving, vertexObserving)) {
                int successor = Graphs.neighborListOf(res, vertexObserving).get(0);
                int predecessor = Graphs.neighborListOf(res, vertexObserving).get(1);

                Set<String> labels = res.getAttributes(vertexObserving).getOrDefault("label", Collections.emptySet());
                MyEdge contracted = contractSingleVertexUndirected(res, vertexObserving);
                Chain chain = new Chain();
                origins.put(chain, new LinkedList<>(List.of(vertexObserving)));
                chain.append(labels);

                while (res.degreeOf(successor) == 2 && !res.containsEdge(successor, successor)) {
                    int finalPredecessor = predecessor;
                    Optional<Integer> newSuccessor = Graphs.neighborListOf(res, successor).stream().filter(x -> x != finalPredecessor).findAny();
                    toContract.remove(successor);
                    origins.get(chain).addLast(successor);
                    labels = res.getAttributes(successor).getOrDefault("label", Collections.emptySet());
                    contracted = contractSingleVertexUndirected(res, successor);
                    chain.append(labels);
                    if (newSuccessor.isPresent()) {
                        successor = newSuccessor.get();
                    } else {
                        break;
                    }
                }
                while (res.degreeOf(predecessor) == 2 && !res.containsEdge(predecessor, predecessor)) {
                    int finalSuccessor = successor;
                    Optional<Integer> newPredecessor = Graphs.neighborListOf(res, predecessor).stream().filter(x -> x != finalSuccessor).findAny();
                    toContract.remove(predecessor);
                    origins.get(chain).addFirst(predecessor);
                    labels = res.getAttributes(predecessor).getOrDefault("label", Collections.emptySet());
                    contracted = contractSingleVertexUndirected(res, predecessor);
                    chain.prepend(labels);
                    if (newPredecessor.isPresent()) {
                        predecessor = newPredecessor.get();
                    } else {
                        break;
                    }
                }
                chains.put(contracted, chain.lock());
            }
        }
        res.setChains(Collections.unmodifiableMap(chains));
        Mapping toReturn = GraphUtil.repairVertices(res);
        return new ContractResult(toReturn.graph, toReturn.newToOld, origins);
    }


    private MyEdge contractSingleVertexDirected(MyGraph graph, int vertex) {
        int predecessor = Graphs.predecessorListOf(graph, vertex).get(0);
        int successor = Graphs.successorListOf(graph, vertex).get(0);
        if (successor == vertex || predecessor==vertex) {
            throw new IllegalStateException("Cannot contract self-loop");
        }
        graph.removeVertex(vertex);
        return graph.addEdge(predecessor, successor);
    }

    private MyEdge contractSingleVertexUndirected(MyGraph graph, int vertex) {
        List<Integer> neighbourList = graph.edgesOf(vertex).stream().map(x -> Graphs.getOppositeVertex(graph, x, vertex)).collect(Collectors.toList());
        assert neighbourList.size() == 2;
        int predecessor = neighbourList.get(0);
        int successor = neighbourList.get(1);
        if (successor == vertex || predecessor == vertex) {
            throw new IllegalStateException("Cannot contract self-loop");
        }
        graph.removeVertex(vertex);
        return graph.addEdge(predecessor, successor);
    }

    private static boolean suitableForContractionDirected(MyGraph graph, int vertex) {
        assert graph.containsVertex(vertex);
        List<Integer> predecessors = Graphs.predecessorListOf(graph, vertex);
        if (predecessors.size() != 1) {
            return false;
        }
        List<Integer> successors = Graphs.successorListOf(graph, vertex);
        return successors.size() == 1 && !predecessors.get(0).equals(vertex) ;
    }


    private static class ContractionResult {
        private final Map<Integer, Integer> newToOld;
        private final MyGraph graph;

        public ContractionResult(MyGraph graph, Map<Integer, Integer> newToOld) {
            this.graph = graph;
            this.newToOld = newToOld;
        }
    }
}