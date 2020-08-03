package com.charrey;

import com.charrey.algorithms.UtilityData;
import com.charrey.algorithms.vertexordering.GreatestConstrainedFirst;
import com.charrey.algorithms.vertexordering.Mapping;
import com.charrey.algorithms.vertexordering.MaxDegreeFirst;
import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.result.*;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.PruningApplicationConstants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;


/**
 * Class that finds node disjoint subgraph homeomorphisms
 */
public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private EdgeMatching edgeMatching;
    private VertexMatching vertexMatching;
    private GlobalOccupation occupation;

    private long lastPrint = System.currentTimeMillis();

    private static boolean allDone(@NotNull MyGraph pattern, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacement().size() == pattern.vertexSet().size();
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

    private static Map<MyEdge, Path> repairPaths(MyGraph newSourceGraph, MyGraph oldTargetGraph, EdgeMatching edgeMatching, int[] sourcegraphNewToOld, VertexMatching vertexMatching, int[] targetgraphNewToOld) {
        Map<MyEdge, Path> res = new HashMap<>();
        if (newSourceGraph.isDirected()) {
            for (MyEdge edge : newSourceGraph.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeTarget(edge));
                Optional<Path> match = edgeMatching.allPaths().stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).findAny();
                assert match.isPresent();
                Path gotten = new Path(oldTargetGraph, targetgraphNewToOld[match.get().first()]);
                for (int i = 1; i < match.get().asList().size(); i++) {
                    gotten.append(targetgraphNewToOld[match.get().get(i)]);
                }
                res.put(new MyEdge(sourcegraphNewToOld[edge.getSource()], sourcegraphNewToOld[edge.getTarget()]), gotten);
            }
        } else {
            for (MyEdge edge : newSourceGraph.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeTarget(edge));
                Optional<Path> match = edgeMatching.allPaths().stream().filter(x -> Set.of(x.last(), x.first()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).findAny();
                assert match.isPresent();
                Path gotten = new Path(oldTargetGraph, targetgraphNewToOld[match.get().first()]);
                for (int i = 1; i < match.get().asList().size(); i++) {
                    gotten.append(targetgraphNewToOld[match.get().get(i)]);
                }
                res.put(new MyEdge(sourcegraphNewToOld[edge.getSource()], sourcegraphNewToOld[edge.getTarget()]), gotten);
            }
        }

        return res;
    }

    private void setup(@NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph, @NotNull Settings settings, String name) throws DomainCheckerException {
        UtilityData data = new UtilityData(sourceGraph, targetGraph);
        if (settings.getWhenToApply() == PruningApplicationConstants.CACHED && Arrays.stream(data.getCompatibility(settings.getFiltering())).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException("Intial domain check failed");
        }
        occupation = new GlobalOccupation(data, settings);
        vertexMatching = new VertexMatching(sourceGraph, targetGraph, occupation, settings);
        edgeMatching = new EdgeMatching(vertexMatching, data, sourceGraph, targetGraph, occupation, settings);
        vertexMatching.setEdgeMatchingProvider(edgeMatching);
    }

    /**
     * Searches for a node disjoint subgraph homeomorphism.
     *
     * @param testcase the case that contains a source graph and a target graph
     * @param settings settings to be used in the search
     * @param timeout  if the algorithm takes longer than this number of milliseconds, it stops and records a failure.
     * @return a result that provides information on the performance and which homeomorphism was found (if any).
     */
    @NotNull
    public HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, @NotNull Settings settings, long timeout, String name) {
        try {
            Mapping sourceGraphMapping;
            Mapping targetGraphMapping;
            MyGraph newSourceGraph;
            MyGraph newTargetGraph;
            try {
                sourceGraphMapping = new GreatestConstrainedFirst().apply(testcase.getSourceGraph());
                newSourceGraph = sourceGraphMapping.graph;
                targetGraphMapping = new MaxDegreeFirst().apply(testcase.getTargetGraph());
                newTargetGraph = targetGraphMapping.graph;
                setup(newSourceGraph, newTargetGraph, settings, name);
            } catch (DomainCheckerException e) {
                return new CompatibilityFailResult();
            }
            long iterations = 0;
            long initialTime = System.currentTimeMillis();
            while (!allDone(newSourceGraph, vertexMatching, edgeMatching)) {
                //System.out.println(vertexMatching);
                //System.out.println(edgeMatching);
                iterations++;
                if (System.currentTimeMillis() - lastPrint > 1000) {
                    long finalIterations = iterations;
                    LOG.info(() -> name + " is at " + finalIterations + " iterations...");
                    lastPrint = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() > initialTime + timeout) {
                    return new TimeoutResult(iterations);
                }
                LOG.fine(() -> vertexMatching.toString() + "\n" + edgeMatching.toString());
                if (edgeMatching.hasUnmatched()) {
                    Path nextpath = edgeMatching.placeNextUnmatched();
                    if (nextpath == null) {
                        if (edgeMatching.retry()) {
                            vertexMatching.giveAllowance();
                        } else {
                            vertexMatching.removeLast();
                        }
                    }
                } else if (vertexMatching.canPlaceNext()) {
                    vertexMatching.placeNext();
                } else if (edgeMatching.retry()) {
                    vertexMatching.giveAllowance();
                } else if (vertexMatching.canRetry()) {
                    vertexMatching.removeLast();
                } else {
                    return new FailResult(iterations);
                }
            }
            if (vertexMatching.getPlacement().size() < testcase.getSourceGraph().vertexSet().size()) {
                return new FailResult(iterations);
            } else {
                int[] placement = vertexMatching.getPlacement().toArray();
                int[] vertexMapping = new int[placement.length];
                for (int i = 0; i < placement.length; i++) {
                    vertexMapping[sourceGraphMapping.newToOld[i]] = targetGraphMapping.newToOld[placement[i]];
                }
                assert allDone(newSourceGraph, vertexMatching, edgeMatching);
                assert Verifier.isCorrect(newSourceGraph, vertexMatching, edgeMatching);


                Map<MyEdge, Path> paths = repairPaths(newSourceGraph, testcase.getTargetGraph(), edgeMatching, sourceGraphMapping.newToOld, vertexMatching, targetGraphMapping.newToOld);
                return new SuccessResult(vertexMapping, paths, iterations);
            }
        } finally {
            if (occupation != null) {
                occupation.close();
            }
        }
    }


}
