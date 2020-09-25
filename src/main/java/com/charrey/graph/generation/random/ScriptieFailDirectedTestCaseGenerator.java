package com.charrey.graph.generation.random;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.util.GraphUtil;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.generate.GnmRandomGraphGenerator;

import java.util.*;
import java.util.function.Consumer;

/**
 * A class that generates random test cases of directed graphs.
 */
public class ScriptieFailDirectedTestCaseGenerator extends TrulyRandomTestCaseGenerator {

    private final double sourceWireFraction = 12d/30d;
    private final double sourceNormalPortFraction = 15d/30d;
    private final double sourceCEPortFraction = 1d/30d;
    private final double sourceSliceFraction = 2d/30d;
    private final double sourceArcFraction = 0d/30d;

    private final double targetWireFraction = 2842d/25288d;
    private final double targetNormalPortFraction = 1107d/25288d;
    private final double targetCEPortFraction = 9d/25288d;
    private final double targetSliceFraction = 36d/25288d;
    private final double targetArcFraction = 21294d/25288d;


    /**
     * Instantiates a new generator.
     *
     * @param patternNodes the number of nodes the source graph should have
     * @param seed         a random seed used to obtain reproducibility.
     */
    public ScriptieFailDirectedTestCaseGenerator(int patternNodes, double nodeFactor, int seed) {
        super(patternNodes, 0, nodeFactor, seed);
    }


    @Override
    public void makeHarder() {
        this.patternNodes++;
        this.targetNodes = (int) Math.ceil(patternNodes * this.getNodeFactor());
    }

    @NotNull
    protected MyGraph randomGraph(int nodes, int edges, boolean source) {
        MyGraph pattern = new MyGraph(true);
        double wireFraction = source ? sourceWireFraction : targetWireFraction;
        double normalPortFraction = source ? sourceNormalPortFraction : targetNormalPortFraction;
        double cePortFraction = source ? sourceCEPortFraction : targetCEPortFraction;
        double sliceFraction = source ? sourceSliceFraction : targetSliceFraction;
        double arcFraction = source ? sourceArcFraction : targetArcFraction;

        List<Integer> logicCells = new ArrayList<>();
        List<Integer> normalPorts = new ArrayList<>();
        List<Integer> cePorts = new ArrayList<>();
        List<Integer> wires = new ArrayList<>();
        List<Integer> arcs = new ArrayList<>();

        int howManyNodesWire = (int) Math.round(wireFraction * nodes);
        for (int i = 0; i < howManyNodesWire; i++) {
            int vertex = pattern.addVertex();
            pattern.addAttribute(vertex, "label", "wire");
            wires.add(vertex);
        }
        int howmanyNodesNormalPort = (int) (Math.round((wireFraction + normalPortFraction) * nodes) - pattern.vertexSet().size());
        for (int i = 0; i < howmanyNodesNormalPort; i++) {
            int vertex = pattern.addVertex();
            pattern.addAttribute(vertex, "label", "port");
            normalPorts.add(vertex);
        }
        int howmanyNodesCEPort = (int) (Math.round((wireFraction + normalPortFraction + cePortFraction) * nodes) - pattern.vertexSet().size());
        for (int i = 0; i < howmanyNodesCEPort; i++) {
            int vertex = pattern.addVertex();
            pattern.addAttribute(vertex, "label", "port");
            pattern.addAttribute(vertex, "label", "CE");
            cePorts.add(vertex);
        }
        int howmanyNodesArc = (int) (Math.round((wireFraction + normalPortFraction + cePortFraction + arcFraction) * nodes) - pattern.vertexSet().size());
        for (int i = 0; i < howmanyNodesArc; i++) {
            int vertex = pattern.addVertex();
            pattern.addAttribute(vertex, "label", "arc");
            arcs.add(vertex);
        }
        int howmanyNodesSlice = Math.max(howmanyNodesNormalPort + howmanyNodesCEPort > 0 ? 1 : 0, nodes - pattern.vertexSet().size());
        for (int i = 0; i < howmanyNodesSlice; i++) {
            int vertex = pattern.addVertex();
            pattern.addAttribute(vertex, "label", "SLICE");
            logicCells.add(vertex);
        }
        normalPorts.forEach(integer -> {
            int randomCell = logicCells.get(random.nextInt(logicCells.size()));
            int randomWire = howManyNodesWire > 0 ? wires.get(random.nextInt(wires.size())) : -1;
            if (random.nextBoolean()) {
                pattern.addEdge(integer, randomCell);
                if (randomWire > -1) {
                    pattern.addEdge(randomWire, integer);
                }
            } else {
                pattern.addEdge(randomCell, integer);
                if (randomWire > -1) {
                    pattern.addEdge(integer, randomWire);
                }
            }
        });
        cePorts.forEach(integer -> {
            pattern.addEdge(integer, logicCells.get(random.nextInt(logicCells.size())));
            if (howManyNodesWire > 0) {
                int randomWire = wires.get(random.nextInt(wires.size()));
                pattern.addEdge(randomWire, integer);
            }
        });
        if (wires.size() >= 2) {
            arcs.forEach(integer -> {
                int wireFrom = wires.get(random.nextInt(wires.size()));
                int wireTo = wires.stream().filter(x -> x != wireFrom).skip(random.nextInt(wires.size() - 1)).findFirst().get();
                pattern.addEdge(wireFrom, integer);
                pattern.addEdge(integer, wireTo);
            });
        }
        return new MyGraph(shuffleIdentifiers(pattern, random));
    }
}
