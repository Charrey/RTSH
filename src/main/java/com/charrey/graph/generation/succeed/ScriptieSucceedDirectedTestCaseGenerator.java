package com.charrey.graph.generation.succeed;

import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.random.ScriptieFailDirectedTestCaseGenerator;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

public class ScriptieSucceedDirectedTestCaseGenerator extends TestCaseGenerator {

    private final double sourceWireFraction = 12d/30d;
    private final double sourceNormalPortFraction = 15d/30d;
    private final double sourceCEPortFraction = 1d/30d;
    private final double sourceSliceFraction = 2d/30d;
    private final double sourceArcFraction = 0d/30d;

    private final double targetSliceFraction = 36d/25288d;

    private final Random random;
    private int patternNodes;
    private final double nodeFactor;
    private int targetNodes;

    public ScriptieSucceedDirectedTestCaseGenerator(int patternNodes, double nodeFactor, int seed) {
        this.random = new Random(seed);
        this.patternNodes = patternNodes;
        this.nodeFactor = nodeFactor;
        this.targetNodes = (int) Math.ceil(patternNodes * nodeFactor);
    }

    @Override
    public void makeHarder() {
        patternNodes++;
        this.targetNodes = (int) Math.ceil(patternNodes * nodeFactor);
    }

    @Override
    protected TestCase getRandom() {
        return getRandomWithSourceGraph(new ScriptieFailDirectedTestCaseGenerator(patternNodes, 0d, random.nextInt()).init(1).getNext().getSourceGraph());
    }


    public TestCase getRandomWithSourceGraph(MyGraph sourceGraph) {
        MyGraph targetGraph = new MyGraph(sourceGraph);

        List<Integer> addedLogicCells = new ArrayList<>();
        List<Integer> addedNormalPorts = new ArrayList<>();
        List<Integer> addedCEPorts = new ArrayList<>();
        List<Integer> addedWires = new ArrayList<>();
        Deque<Integer> addedArcs = new LinkedList<>();
        int totalAdded = 0;

        double targetWireFraction = 2842d / 25288d;
        int howManyNodesWire = (int) Math.round(targetWireFraction * targetNodes);
        int howManyExtraNodesWire = Math.max(0, howManyNodesWire - (int) sourceGraph.vertexSet().stream().filter(x -> sourceGraph.getLabels(x).contains("wire")).count());
        for (int i = 0; i < howManyExtraNodesWire; i++) {
            int vertex = targetGraph.addVertex();
            targetGraph.addAttribute(vertex, "label", "wire");
            addedWires.add(vertex);
            totalAdded++;
        }
        double targetNormalPortFraction = 1107d / 25288d;
        int howManyNodesNormalPort = (int) (Math.round((targetWireFraction + targetNormalPortFraction) * targetNodes) - totalAdded);
        int howManyExtraNodesNormalPort = Math.max(0, howManyNodesNormalPort - (int) sourceGraph.vertexSet().stream().filter(integer -> sourceGraph.getLabels(integer).contains("port") && !sourceGraph.getLabels(integer).contains("CE")).count());
        for (int i = 0; i < howManyExtraNodesNormalPort; i++) {
            int vertex = targetGraph.addVertex();
            targetGraph.addAttribute(vertex, "label", "port");
            addedNormalPorts.add(vertex);
            totalAdded++;
        }

        double targetCEPortFraction = 9d / 25288d;
        int howmanyNodesCEPort = (int) (Math.round((targetWireFraction + targetNormalPortFraction + targetCEPortFraction) * targetNodes) - totalAdded);
        int howManyExtraNodesCEPort = Math.max(0, howmanyNodesCEPort - (int) sourceGraph.vertexSet().stream().filter(integer -> sourceGraph.getLabels(integer).contains("port") && sourceGraph.getLabels(integer).contains("CE")).count());
        for (int i = 0; i < howManyExtraNodesCEPort; i++) {
            int vertex = targetGraph.addVertex();
            targetGraph.addAttribute(vertex, "label", "port");
            targetGraph.addAttribute(vertex, "label", "CE");
            addedCEPorts.add(vertex);
            totalAdded++;
        }
        double targetArcFraction = 21294d / 25288d;
        int howmanyNodesArc = (int) (Math.round((targetWireFraction + targetNormalPortFraction + targetCEPortFraction + targetArcFraction) * targetNodes) - totalAdded);
        int howManyExtraNodesArc = Math.max(0, howmanyNodesArc - (int) sourceGraph.vertexSet().stream().filter(integer -> sourceGraph.getLabels(integer).contains("arc")).count());
        for (int i = 0; i < howManyExtraNodesArc; i++) {
            int vertex = targetGraph.addVertex();
            targetGraph.addAttribute(vertex, "label", "arc");
            addedArcs.add(vertex);
            totalAdded++;
        }
        int howmanyNodesSlice = Math.max(howManyNodesNormalPort + howmanyNodesCEPort > 0 ? 1 : 0, targetNodes - totalAdded);
        int howManyExtraNodesSlice = Math.max(0, howmanyNodesSlice - (int) sourceGraph.vertexSet().stream().filter(integer -> sourceGraph.getLabels(integer).contains("SLICE")).count());
        if (targetGraph.vertexSet().stream().noneMatch(x -> targetGraph.getLabels(x).contains("SLICE")) && addedCEPorts.size() + addedNormalPorts.size() > 0) {
            howManyExtraNodesSlice++;
        }
        for (int i = 0; i < howManyExtraNodesSlice; i++) {
            int vertex = targetGraph.addVertex();
            targetGraph.addAttribute(vertex, "label", "SLICE");
            addedLogicCells.add(vertex);
            totalAdded++;
        }
        connectPorts(sourceGraph, targetGraph, addedLogicCells, addedNormalPorts, addedCEPorts);
        intersect(targetGraph, addedWires, addedArcs);
        List<Integer> allWires = targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("wire")).sorted().collect(Collectors.toList());
        if (allWires.size() >= 2) {
            while (!addedArcs.isEmpty()) {
                int arc = addedArcs.poll();
                int randomWireSource;
                if (!addedWires.isEmpty() && addedWires.stream().mapToDouble(targetGraph::degreeOf).average().getAsDouble() <
                    targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("wire")).mapToDouble(targetGraph::degreeOf).average().getAsDouble()) {
                    randomWireSource = addedWires.get(random.nextInt(addedWires.size()));
                } else {
                    randomWireSource = allWires.get(random.nextInt(allWires.size()));
                }
                int randomWireTarget = allWires.stream().filter(x -> x != randomWireSource).skip(random.nextInt(allWires.size() - 1)).findFirst().get();
                targetGraph.addEdge(randomWireSource, arc);
                targetGraph.addEdge(arc, randomWireTarget);
            }
        }
        return new TestCase(shuffleIdentifiers(sourceGraph, random), shuffleIdentifiers(targetGraph, random), null, null);
    }

    private void intersect(MyGraph targetGraph, List<Integer> addedWires, Deque<Integer> addedArcs) {
        Deque<Integer> toIntersect = addedWires.stream().skip(addedWires.size() - Math.round(addedWires.size() / 2d)).sorted().collect(Collectors.toCollection(LinkedList::new));
        while (toIntersect.size() > addedArcs.size()) {
            toIntersect.poll();
        }
        while (!toIntersect.isEmpty()) {
            int wireToIntersectWith = toIntersect.poll();
            int arcBuddy = addedArcs.poll();
            int arcWireCandidates = (int) targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("arc")).count();
            int portCandidates = (int) targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("port")).count();
            double arcProbability = arcWireCandidates / (double)(arcWireCandidates + portCandidates);
            List<Integer> allArcs = targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("arc")).sorted().collect(Collectors.toList());
            List<Integer> allPorts = targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("port")).sorted().collect(Collectors.toList());

            boolean arcsSuitable = allArcs.stream().noneMatch(x -> Graphs.predecessorListOf(targetGraph, x).isEmpty());
            boolean portsSuitable = allPorts.size() > 0 && allPorts.stream().noneMatch(x -> Graphs.predecessorListOf(targetGraph, x).isEmpty());
            if (random.nextDouble() < arcProbability && arcsSuitable) {
                //intersect arc
                int randomArc = allArcs.get(random.nextInt(allArcs.size()));
                int successor = Graphs.successorListOf(targetGraph, randomArc).get(0);
                targetGraph.removeEdge(randomArc, successor);
                targetGraph.addEdge(randomArc, wireToIntersectWith);
                targetGraph.addEdge(wireToIntersectWith, arcBuddy);
                targetGraph.addEdge(arcBuddy, successor);
            } else if (portsSuitable) {
                //intersect port
                int randomPort = allPorts.get(random.nextInt(allPorts.size()));
                int predecessor = Graphs.predecessorListOf(targetGraph, randomPort).get(0);
                if (targetGraph.getLabels(predecessor).contains("SLICE")){
                    //flow towards successor
                    int successor = Graphs.successorListOf(targetGraph, randomPort).get(0);
                    targetGraph.removeEdge(randomPort, successor);
                    targetGraph.addEdge(randomPort, wireToIntersectWith);
                    targetGraph.addEdge(wireToIntersectWith, arcBuddy);
                    targetGraph.addEdge(arcBuddy, successor);
                } else {
                    //flow towards logic cell
                    targetGraph.removeEdge(predecessor, randomPort);
                    targetGraph.addEdge(predecessor, arcBuddy);
                    targetGraph.addEdge(arcBuddy, wireToIntersectWith);
                    targetGraph.addEdge(wireToIntersectWith, randomPort);
                }
            }
        }
    }

    private void connectPorts(MyGraph sourceGraph, MyGraph targetGraph, List<Integer> addedLogicCells, List<Integer> addedNormalPorts, List<Integer> addedCEPorts) {
        Deque<Integer> toConnectToLogicCell = new LinkedList<>(addedNormalPorts);
        List<Integer> allWires = targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("wire")).sorted().collect(Collectors.toList());
        List<Integer> allLogicCells = targetGraph.vertexSet().stream().filter(x -> targetGraph.getLabels(x).contains("SLICE")).sorted().collect(Collectors.toList());
        while (!addedLogicCells.isEmpty() &&
                !toConnectToLogicCell.isEmpty() &&
                addedLogicCells.stream().map(targetGraph::degreeOf).mapToDouble(x -> x).average().getAsDouble() <
                        sourceGraph.vertexSet().stream().filter(x -> sourceGraph.getLabels(x).contains("SLICE")).map(sourceGraph::degreeOf).mapToDouble(x -> x).average().getAsDouble()) {
            int randomAddedLogicCell = addedLogicCells.get(random.nextInt(addedLogicCells.size()));
            int portToConnect = toConnectToLogicCell.poll();
            if (random.nextBoolean()) {
                targetGraph.addEdge(portToConnect, randomAddedLogicCell);
                if (!allWires.isEmpty()) {
                    int randomWire = allWires.get(random.nextInt(allWires.size()));
                    targetGraph.addEdge(randomWire, portToConnect);
                }
            } else {
                targetGraph.addEdge(randomAddedLogicCell, portToConnect);
                if (!allWires.isEmpty()) {
                    int randomWire = allWires.get(random.nextInt(allWires.size()));
                    targetGraph.addEdge(portToConnect, randomWire);
                }
            }
        }
        while (!toConnectToLogicCell.isEmpty()) {
            int randomLogicCell = allLogicCells.get(random.nextInt(allLogicCells.size()));
            int portToConnect = toConnectToLogicCell.poll();
            if (random.nextBoolean()) {
                targetGraph.addEdge(portToConnect, randomLogicCell);
                if (!allWires.isEmpty()) {
                    int randomWire = allWires.get(random.nextInt(allWires.size()));
                    targetGraph.addEdge(randomWire, portToConnect);
                }
            } else {
                targetGraph.addEdge(randomLogicCell, portToConnect);
                if (!allWires.isEmpty()) {
                    int randomWire = allWires.get(random.nextInt(allWires.size()));
                    targetGraph.addEdge(portToConnect, randomWire);
                }
            }
        }
        toConnectToLogicCell = new LinkedList<>(addedCEPorts);
        while (!addedLogicCells.isEmpty() &&
                !toConnectToLogicCell.isEmpty() &&
                addedLogicCells.stream().map(targetGraph::degreeOf).mapToDouble(x -> x).average().getAsDouble() <
                        sourceGraph.vertexSet().stream().filter(x -> sourceGraph.getLabels(x).contains("SLICE")).map(sourceGraph::degreeOf).mapToDouble(x -> x).average().getAsDouble()) {
            int randomAddedLogicCell = addedLogicCells.get(random.nextInt(addedLogicCells.size()));
            int portToConnect = toConnectToLogicCell.poll();
            targetGraph.addEdge(portToConnect, randomAddedLogicCell);
            if (!allWires.isEmpty()) {
                int randomWire = allWires.get(random.nextInt(allWires.size()));
                targetGraph.addEdge(randomWire, portToConnect);
            }
        }
        while (!toConnectToLogicCell.isEmpty()) {
            int randomLogicCell = allLogicCells.get(random.nextInt(allLogicCells.size()));
            int portToConnect = toConnectToLogicCell.poll();
            targetGraph.addEdge(portToConnect, randomLogicCell);
            if (!allWires.isEmpty()) {
                int randomWire = allWires.get(random.nextInt(allWires.size()));
                targetGraph.addEdge(randomWire, portToConnect);
            }
        }
    }
}
