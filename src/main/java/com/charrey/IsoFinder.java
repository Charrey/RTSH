package com.charrey;

import com.charrey.algorithms.GreatestConstrainedFirst;
import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.TestCase;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.result.*;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Logger;


/**
 * Class that finds node disjoint subgraph homeomorphisms
 */
public class IsoFinder {

    private static final Logger LOG = Logger.getLogger("IsoFinder");
    private EdgeMatching edgeMatching;
    private VertexMatching vertexMatching;

    private static void logDomainReduction(@NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph, @NotNull UtilityData data, FilteringSettings filteringSettings, String name) {
        BigInteger naiveVertexDomainSize = new BigInteger(String.valueOf(sourceGraph.vertexSet().size())).pow(targetGraph.vertexSet().size());
        BigInteger vertexDomainSize = Arrays.stream(data.getCompatibility(filteringSettings, name)).reduce(new BigInteger("1"), (i, vs) -> i.multiply(new BigInteger(String.valueOf(vs.length))), BigInteger::multiply);
        NumberFormat formatter = new DecimalFormat("0.###E0", DecimalFormatSymbols.getInstance(Locale.ROOT));
        LOG.info(() -> "Reduced vertex matching domain from " + formatter.format(naiveVertexDomainSize) + " to " + formatter.format(vertexDomainSize));
    }

    private long lastPrint = 0;

    private static boolean allDone(@NotNull MyGraph pattern, @NotNull VertexMatching vertexMatching, @NotNull EdgeMatching edgeMatching) {
        boolean completeV = vertexMatching.getPlacement().size() == pattern.vertexSet().size();
        if (!completeV) {
            return false;
        }
        boolean completeE = !edgeMatching.hasUnmatched();
        if (!completeE) {
            return false;
        }
        LOG.info(() -> "Done, checking...");
        boolean correct = Verifier.isCorrect(pattern, vertexMatching, edgeMatching);
        assert correct;
        return true;
    }

    private static Map<MyEdge, Path> repairPaths(MyGraph oldSourceGraph, MyGraph newSourceGraph, EdgeMatching edgeMatching, int[] new_to_old, VertexMatching vertexMatching) {
        Map<MyEdge, Path> res = new HashMap<>();
        if (newSourceGraph.isDirected()) {
            for (MyEdge edge : newSourceGraph.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeTarget(edge));
                Optional<Path> match = edgeMatching.allPaths().stream().filter(x -> x.last() == edgeTargetTarget && x.first() == edgeSourceTarget).findAny();
                assert match.isPresent();
                res.put(new MyEdge(new_to_old[edge.source], new_to_old[edge.target]), match.get());
            }
        } else {
            for (MyEdge edge : newSourceGraph.edgeSet()) {
                int edgeSourceTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeSource(edge));
                int edgeTargetTarget = vertexMatching.getPlacement().get(newSourceGraph.getEdgeTarget(edge));
                Optional<Path> match = edgeMatching.allPaths().stream().filter(x -> Set.of(x.last(), x.first()).equals(Set.of(edgeSourceTarget, edgeTargetTarget))).findAny();
                assert match.isPresent();
                res.put(new MyEdge(new_to_old[edge.source], new_to_old[edge.target]), match.get());
            }
        }

        return res;
    }

    private void setup(@NotNull MyGraph sourceGraph, @NotNull MyGraph targetGraph, @NotNull Settings settings, String name) throws DomainCheckerException {
        UtilityData data = new UtilityData(sourceGraph, targetGraph);
        logDomainReduction(sourceGraph, targetGraph, data, settings.filtering, name);

        if (Arrays.stream(data.getCompatibility(settings.filtering, name)).anyMatch(x -> x.length == 0)) {
            throw new DomainCheckerException("Intial domain check failed");
        }
        GlobalOccupation occupation = new GlobalOccupation(data, settings.pruningMethod, settings.filtering, settings.whenToApply, name);
        vertexMatching = new VertexMatching(data, sourceGraph, targetGraph, occupation, settings.filtering, name);
        edgeMatching = new EdgeMatching(vertexMatching, data, sourceGraph, targetGraph, occupation, settings.pathIteration, settings.refuseLongerPaths);
    }

    private static int[] repairMatching(int[] placement, int[] new_to_old) {
        int[] res = new int[placement.length];
        for (int i = 0; i < placement.length; i++) {
            res[i] = placement[new_to_old[i]];
        }
        return res;
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
        GreatestConstrainedFirst.Mapping mapping;
        MyGraph newSourceGraph;
        try {
            mapping = GreatestConstrainedFirst.apply(testcase.getSourceGraph());
            newSourceGraph = mapping.graph;
            setup(newSourceGraph, testcase.getTargetGraph(), settings, name);
        } catch (DomainCheckerException e) {
            return new CompatibilityFailResult();
        }
        long iterations = 0;
        long initialTime = System.currentTimeMillis();
        while (!allDone(newSourceGraph, vertexMatching, edgeMatching)) {
            iterations++;
            if (System.currentTimeMillis() - lastPrint > 1000) {
                LOG.info(name + " is at " + iterations + " iterations...");
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
            int[] vertexMapping = vertexMatching.getPlacement().stream().mapToInt(x -> x).toArray();
            //int[] fixed = repairMatching(vertexMapping, mapping.new_to_old);
            assert allDone(newSourceGraph, vertexMatching, edgeMatching);
            assert Verifier.isCorrect(newSourceGraph, vertexMatching, edgeMatching);


            Map<MyEdge, Path> paths = repairPaths(testcase.getSourceGraph(), newSourceGraph, edgeMatching, mapping.new_to_old, vertexMatching);
            return new SuccessResult(vertexMapping, paths, iterations);
        }
    }


}
