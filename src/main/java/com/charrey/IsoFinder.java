package com.charrey;

import com.charrey.algorithms.UtilityData;
import com.charrey.algorithms.vertexordering.GreatestConstrainedFirst;
import com.charrey.algorithms.vertexordering.Mapping;
import com.charrey.algorithms.vertexordering.MaxDegreeFirst;
import com.charrey.algorithms.vertexordering.RandomOrder;
import com.charrey.graph.*;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.result.*;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.WhenToApply;
import gnu.trove.list.TIntList;
import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Class that finds node disjoint subgraph homeomorphisms
 */
public class IsoFinder implements HomeomorphismSolver {

    private final Settings settings;

    public IsoFinder(Settings settings) {
        this.settings = settings;
    }

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private EdgeMatching edgeMatching;
    private VertexMatching vertexMatching;
    private GlobalOccupation occupation;

    private long lastPrint = System.currentTimeMillis();

    private static boolean allDone(@NotNull MyGraph pattern, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.size() == pattern.vertexSet().size();
        if (!completeV) {
            return false;
        }
        boolean completeE = !edgeMatching.hasUnmatched();
        if (!completeE) {
            return false;
        }
        boolean correct = Verifier.isCorrect(pattern, vertexMatching, edgeMatching);
        assert correct;
        return true;
    }

    private static Map<MyEdge, Set<Path>> repairPaths(MyGraph oldSourceGraph,
                                                      MyGraph oldTargetGraph,
                                                      Set<Path> allPaths,
                                                      List<Integer> placementOldOnOld,
                                                      Map<Integer, Integer> targetgraphNewToOld) {
        //problem: all paths is 1, 0, should be 1, 2
        Map<Integer, Integer> targetgraphOldToNew = new HashMap<>();
        targetgraphNewToOld.forEach((a, b) -> targetgraphOldToNew.put(b, a));
        Map<MyEdge, Set<Path>> res = new HashMap<>();
        for (MyEdge edge : oldSourceGraph.edgeSet()) {
            int edgeSourceTarget = targetgraphOldToNew.get(placementOldOnOld.get(edge.getSource()));
            int edgeTargetTarget = targetgraphOldToNew.get(placementOldOnOld.get(edge.getTarget()));
            Set<Path> toAdd = new HashSet<>();
            Set<Path> match = allPaths.stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).collect(Collectors.toSet());
            assert !match.isEmpty();
            match.forEach(path -> {
                Path gotten = new Path(oldTargetGraph, targetgraphNewToOld.get(path.first()));
                for (int i = 1; i < path.asList().size(); i++) {
                    gotten.append(targetgraphNewToOld.get(path.get(i)));
                }
                toAdd.add(gotten);
            });
            res.put(new MyEdge(edge.getSource(), edge.getTarget()), toAdd);
        }
        return res;
    }

    private void setup(@NotNull MyGraph sourceGraph,
                       @NotNull MyGraph targetGraph,
                       @NotNull Settings settings,
                       long timeoutTime) throws DomainCheckerException {
        UtilityData data = new UtilityData(sourceGraph, targetGraph);
        if (settings.getWhenToApply() == WhenToApply.CACHED && Arrays.stream(data.getCompatibility(settings.getFiltering())).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException(() -> "Intial domain check failed");
        }
        occupation = new GlobalOccupation(data, settings);
        vertexMatching = new VertexMatching(sourceGraph, targetGraph, occupation, settings);
        occupation.init(vertexMatching);
        edgeMatching = new EdgeMatching(vertexMatching, data, sourceGraph, targetGraph, occupation, settings, timeoutTime);
        vertexMatching.setEdgeMatchingProvider(edgeMatching);
    }

    /**
     * Searches for a node disjoint subgraph homeomorphism.
     *
     * @param testcase the case that contains a source graph and a target graph
     * @param timeout  if the algorithm takes longer than this number of milliseconds, it stops and records a failure.
     * @param monitorSpace
     * @return a result that provides information on the performance and which homeomorphism was found (if any).
     */
    @NotNull
    public HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, long timeout, String name, boolean monitorSpace) {
        long timeoutTime = System.currentTimeMillis() + timeout;
        Settings tempSettings = settings.newInstance();
        double mem = Runtime.getRuntime().totalMemory();
        long lastSpaceMeasure = 0L;
        if (monitorSpace) {
            lastSpaceMeasure = System.currentTimeMillis();
        }

        try {
            Mapping sourceGraphMapping;
            Mapping targetGraphMapping;
            MyGraph newSourceGraph = testcase.getSourceGraph();
            MyGraph newTargetGraph = testcase.getTargetGraph();
            ContractResult contractMapping = null;
            if (tempSettings.getContraction()) {
                contractMapping =  newSourceGraph.contract();
                newSourceGraph = contractMapping.getGraph();
            }
            try {
                sourceGraphMapping = switch (tempSettings.getSourceVertexOrder()) {
                    case GREATEST_CONSTRAINED_FIRST -> new GreatestConstrainedFirst().apply(newSourceGraph);
                    case RANDOM -> new RandomOrder(tempSettings.nextLong()).apply(newSourceGraph);
                };
                newSourceGraph = sourceGraphMapping.graph;
                targetGraphMapping = new MaxDegreeFirst().apply(newTargetGraph);
                newTargetGraph = targetGraphMapping.graph;
                setup(newSourceGraph, newTargetGraph, tempSettings, timeoutTime);
            } catch (DomainCheckerException e) {
                if (monitorSpace) {
                    mem = Math.max(mem, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                }
                return new CompatibilityFailResult(mem);
            }
            long iterations = 0;
            boolean iterationpassed = true;

            while (!allDone(newSourceGraph, vertexMatching, edgeMatching)) {
                if (monitorSpace && System.currentTimeMillis() - lastSpaceMeasure > 100) {
                    mem = Math.max(mem, Runtime.getRuntime().totalMemory());
                }
                if (iterationpassed) {
                    iterations = logProgress(iterations);
                }
                iterationpassed = false;
                if (System.currentTimeMillis() > timeoutTime || Thread.interrupted()) {
                    return new TimeoutResult(iterations, mem);
                }
                if (edgeMatching.hasUnmatched()) {
                    Path nextpath = edgeMatching.placeNextUnmatched();
                    if (nextpath == null) {
                        if (edgeMatching.retry()) {
                            vertexMatching.giveAllowance();
                            iterationpassed = true;
                        } else {
                            vertexMatching.removeLast();
                        }
                    }
                } else if (vertexMatching.canPlaceNext()) {
                    iterationpassed = vertexMatching.placeNext();
                } else if (edgeMatching.retry()) {
                    vertexMatching.giveAllowance();
                    iterationpassed = true;
                } else if (vertexMatching.canRetry()) {
                    vertexMatching.removeLast();
                } else {
                    if (System.currentTimeMillis() >= timeoutTime || Thread.currentThread().isInterrupted()) {
                        return new TimeoutResult(iterations, mem);
                    } else {
                        return new FailResult(iterations, mem);
                    }
                }

            }
            return createResult(testcase, timeoutTime, tempSettings, mem, sourceGraphMapping, targetGraphMapping, newSourceGraph, newTargetGraph, contractMapping, iterations);
        } finally {
            if (occupation != null) {
                occupation.close();
            }
        }
    }

    private HomeomorphismResult createResult(TestCase testcase,
                                             long timeoutTime,
                                             Settings tempSettings,
                                             double mem,
                                             Mapping sourceGraphMapping,
                                             Mapping targetGraphMapping,
                                             MyGraph newSourceGraph,
                                             MyGraph newTargetGraph,
                                             ContractResult contractMapping,
                                             long iterations) {
        if (vertexMatching.size() < newSourceGraph.vertexSet().size()) {
            if (System.currentTimeMillis() >= timeoutTime || Thread.currentThread().isInterrupted()) {
                return new TimeoutResult(iterations, mem);
            } else {
                return new FailResult(iterations, mem);
            }
        } else {
            List<Integer> placementOldOnOld;
            Set<Path> allPaths = new HashSet<>(edgeMatching.allPaths());
            if (tempSettings.getContraction()) {
                List<Integer> placementNewOnNew = vertexMatching.get();
                placementOldOnOld = fixContraction(testcase.getSourceGraph(),
                        newSourceGraph,
                        sourceGraphMapping,
                        contractMapping,
                        placementNewOnNew,
                        edgeMatching,
                        targetGraphMapping);
                assert  !placementOldOnOld.contains(-1);
//  placementOldOnOld = new ArrayList<>();
//                for (int i = 0; i < placementNewOnOld.size(); i++) {
//                    while (placementOldOnOld.size() < sourceGraphMapping.newToOld.get(i) + 1) {
//                        placementOldOnOld.add(-1);
//                    }
//                    placementOldOnOld.set(sourceGraphMapping.newToOld.get(i), placementNewOnOld.get(i));
//                }
                //should be 0 4 1
                List<Integer> finalPlacementOldOnOld = placementOldOnOld;
                Set<Integer> contractedTarget = IntStream.range(0, placementOldOnOld.size())
                        .filter(x -> !contractMapping.getOldToNew().containsKey(x))
                        .map(x -> targetGraphMapping.oldToNew.get(finalPlacementOldOnOld.get(x)))
                        .boxed()
                        .collect(Collectors.toSet());
                allPaths = splitPaths(allPaths, contractedTarget);
            } else {
                placementOldOnOld = new ArrayList<>();
                for (int i = 0; i < vertexMatching.get().size(); i++) {
                    while (placementOldOnOld.size() < sourceGraphMapping.newToOld.get(i) + 1) {
                        placementOldOnOld.add(-1);
                    }
                    placementOldOnOld.set(sourceGraphMapping.newToOld.get(i), targetGraphMapping.newToOld.get(vertexMatching.get().get(i)));
                }
            }
            assert !placementOldOnOld.contains(-1);
            assert allDone(newSourceGraph, vertexMatching, edgeMatching);
            assert Verifier.isCorrect(newSourceGraph, vertexMatching, edgeMatching);
            Map<MyEdge, Set<Path>> paths = repairPaths(testcase.getSourceGraph(),
                    testcase.getTargetGraph(),
                    allPaths,
                    placementOldOnOld,
                    targetGraphMapping.newToOld);
            return new SuccessResult(placementOldOnOld.stream().mapToInt(x -> x).toArray(), paths, iterations, mem);
        }
    }

    private Set<Path> splitPaths(Set<Path> toChange, Collection<Integer> placement) {
        Set<Path> res = new HashSet<>();
        for (Path path : toChange) {
            TIntList list = path.asList();
            int highestThatMayBeIncluded = 0;
            for (int i = 0; i < list.size(); i++) {
                if (i != 0 && i != list.size() - 1 && placement.contains(list.get(i))) {
                    res.add(new Path(path.getGraph(), list.subList(highestThatMayBeIncluded, i + 1)));
                    highestThatMayBeIncluded = i;
                }
            }
            res.add(new Path(path.getGraph(), list.subList(highestThatMayBeIncluded, list.size())));
        }
        return res;
    }

    private List<Integer> fixContraction(MyGraph oldSourceGraph, MyGraph newSourceGraph, Mapping sourceGraphMapping, ContractResult contractResult, List<Integer> placementNewOnNew, EdgeMatching edgeMatching, Mapping targetGraphMapping) {
        List<Integer> placementOldOnOld = new ArrayList<>(oldSourceGraph.vertexSet().size());
        for (int i = 0; i < oldSourceGraph.vertexSet().size(); i++) {
            placementOldOnOld.add(-1);
        }
        Set<Path> allPaths = edgeMatching.allPaths();

        Map<Integer, Integer> combinedOldToNew = new HashMap<>();
        oldSourceGraph.vertexSet().forEach(integer -> combinedOldToNew.put(integer, sourceGraphMapping.oldToNew.get(contractResult.getOldToNew().get(integer))));
        combinedOldToNew.forEach((oldV, newV) -> {
            if (newV != null) {
                int newTargetVertex = placementNewOnNew.get(newV);
                int oldTargetVertex = targetGraphMapping.newToOld.get(newTargetVertex);
                placementOldOnOld.set(oldV, oldTargetVertex);
            }
        });

        Map<Pair<Integer, Integer>, Set<List<Integer>>> newSourcePairToContractlist = new HashMap<>(); //new to old
        newSourceGraph.getAllChains().forEach((key, value) -> {
            newSourcePairToContractlist.computeIfAbsent(new Pair<>(key.getSource(), key.getTarget()), x -> new HashSet<>());
            newSourcePairToContractlist.get(new Pair<>(key.getSource(), key.getTarget())).add(contractResult.getOrigins().get(value));
        });
        Map<Pair<Integer, Integer>, Set<Path>> edgeToPathList = new HashMap<>(); //new to new
        newSourcePairToContractlist.keySet().forEach(pair -> edgeToPathList.put(pair, allPaths.stream().filter(x -> x.first() == placementNewOnNew.get(pair.getFirst()) && x.last() == placementNewOnNew.get(pair.getSecond())).collect(Collectors.toSet())));

        org.chocosolver.solver.Settings settings = new DefaultSettings();
        edgeToPathList.keySet().forEach(oldSourceEdge -> {
            List<List<Integer>> contractList = new ArrayList<>(newSourcePairToContractlist.get(oldSourceEdge));
            List<Path> pathList = new ArrayList<>(edgeToPathList.get(oldSourceEdge));
            Model model = new Model(settings);
            IntVar[] variables = new IntVar[contractList.size()];
            Map<Integer, Map<Integer, Map<Integer, Integer>>> variableToPathindexToAssignment = new HashMap<>();
            for (int i = 0; i < contractList.size(); i++) { //for each contracted sequence of vertices
                List<Integer> listOfContractedVertexChains = contractList.get(i);
                variableToPathindexToAssignment.put(i, new HashMap<>());
                List<Integer> compatiblePaths = new ArrayList<>();
                for (int j = 0; j < pathList.size(); j++) {
                    //map from OLD source to NEW target

                    MyGraph targetGraph = (MyGraph) pathList.get(j).getGraph();
                    TIntList intermediateOfPath = pathList.get(j).intermediate().asList();
                    Map<Integer, Integer> addedMap = chainCompatible(oldSourceGraph,
                            listOfContractedVertexChains,
                            targetGraph,
                            intermediateOfPath);
                    if (addedMap != null) {
                        variableToPathindexToAssignment.get(i).put(j, addedMap);
                        compatiblePaths.add(j);
                    }
                }
                assert compatiblePaths.size() > 0;
                variables[i] = model.intVar(compatiblePaths.stream().mapToInt(x -> x).toArray());
            }
            model.allDifferent(variables).post();
            model.getSolver().solve();
            for (int i = 0; i < variables.length; i++) {
                for (Map.Entry<Integer, Integer> entry : variableToPathindexToAssignment.get(i).get(variables[i].getValue()).entrySet()) {
                    Integer key = entry.getKey();
                    Integer value = entry.getValue();
                    placementOldOnOld.set(key, targetGraphMapping.newToOld.get(value));
                }
            }
        });
        return placementOldOnOld;
    }

    private Map<Integer, Integer> chainCompatible(MyGraph oldGraph, List<Integer> contractedSequence, MyGraph targetGraph, TIntList pathInsideContent) {
        if (contractedSequence.isEmpty()) {
            return new HashMap<>();
        } else if (pathInsideContent.size() < contractedSequence.size()) {
            return null;
        } else {
            if (targetGraph.getLabels(pathInsideContent.get(0)).containsAll(oldGraph.getLabels(contractedSequence.get(0)))) {
                Map<Integer, Integer> recursive = chainCompatible(oldGraph, contractedSequence.subList(1, contractedSequence.size()), targetGraph, pathInsideContent.subList(1, pathInsideContent.size()));
                if (recursive != null) {
                    recursive.put(contractedSequence.get(0), pathInsideContent.get(0));
                }
                return recursive;
            } else {
                return chainCompatible(oldGraph, contractedSequence, targetGraph, pathInsideContent.subList(1, pathInsideContent.size()));
            }
        }
    }

    private long logProgress(long iterations) {
        iterations++;
        if (System.currentTimeMillis() - lastPrint > 1000) {
            lastPrint = System.currentTimeMillis();
        }
        LOG.fine(() -> vertexMatching.toString() + "\n" + edgeMatching.toString());
        return iterations;
    }


}
