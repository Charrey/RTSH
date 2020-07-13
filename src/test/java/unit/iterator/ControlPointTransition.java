package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.Settings;
import com.charrey.settings.iterator.ControlPointIteratorStrategy;
import com.charrey.settings.pruning.PruningApplicationConstants;
import com.charrey.settings.pruning.PruningConstants;
import com.charrey.settings.pruning.domainfilter.LabelDegreeFiltering;
import com.charrey.util.Util;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlPointTransition {

    private final RandomGenerator random = new Well512a(19247);
    private final static int differentGraphSizes = 250;
    private final static int trials = 10;

    private final Settings settings = new Settings(
            new LabelDegreeFiltering(),
            true,
            PruningConstants.NONE,
            new ControlPointIteratorStrategy(3), PruningApplicationConstants.SERIAL);


    @Test
    @Order(1)
    void TestNoMore() throws DomainCheckerException {
        test(myCase -> {
            final Optional<Integer>[] counterExample = new Optional[]{Optional.empty()};
            TIntSet routingOccupied = myCase.globalOccupation.getRoutingOccupied();
            routingOccupied.forEach(new TIntProcedure() {
                @Override
                public boolean execute(int x) {
                    if (myCase.path.intermediate().stream().mapToInt(y -> y).noneMatch(y -> y == x)) {
                        counterExample[0] = Optional.of(x);
                        return false;
                    }
                    return true;
                }
            });
            if (counterExample[0].isPresent()) {
                System.err.println("In occupation but not in path: " + counterExample[0]);
                return false;
            }
            return true;
        });
    }

    @Test
    @Order(2)
    void TestControlPointsOccupied() throws DomainCheckerException {
        test(myCase -> myCase.controlPoints.stream().allMatch(myCase.globalOccupation::isOccupiedRouting));
    }

    @Test
    @Order(3)
    void TestFinalPathOccupied() throws DomainCheckerException {
        test(myCase -> {
            Optional<Integer> counterExample = myCase.finalPath.intermediate().stream().filter(x -> !myCase.globalOccupation.isOccupiedRouting(x)).findAny();
            if (counterExample.isPresent()) {
                System.err.println("Final path: " + myCase.finalPath);
                System.err.println("Occupationbits: " + myCase.globalOccupation.getRoutingOccupied());
                System.err.println("Counterexample: " + counterExample.get());
                return false;
            } else {
                return true;
            }
        });
    }

    @Test
    @Order(4)
    void TestFirstPathOccupied() throws DomainCheckerException {
        test(myCase -> {
            Optional<Integer> counterExample = myCase.firstPath.intermediate().stream().filter(x -> !myCase.globalOccupation.isOccupiedRouting(x)).findAny();
            if (counterExample.isPresent()) {
                System.err.println("Final path: " + myCase.firstPath);
                System.err.println("Occupationbits: " + myCase.globalOccupation.getRoutingOccupied());
                System.err.println("Counterexample: " + counterExample.get());
                return false;
            } else {
                return true;
            }
        });
    }

    private void test(Predicate<Case> toTest) throws DomainCheckerException {
        final long seed = 1923;
        long counter = -1;
        RandomSucceedDirectedTestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator(2, 1, 0, 0, seed);
        for (int i = 0; i < differentGraphSizes; i++) {
            gen.makeHarder();
            gen.init(trials, false);
            while (gen.hasNext()) {
                MyGraph targetGraph = gen.getNext().getSourceGraph();
                MyGraph sourceGraph = new MyGraph(true);
                sourceGraph.addEdge(sourceGraph.addVertex(), sourceGraph.addVertex());
                UtilityData data = new UtilityData(sourceGraph, targetGraph);
                int tail = Util.selectRandom(targetGraph.vertexSet(), x -> true, random);
                int head = Util.selectRandom(targetGraph.vertexSet(), x -> x != tail, random);
                counter++;
                System.out.print(counter % 100 == 0 ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                GlobalOccupation occupation = new GlobalOccupation(data, settings, "TransitionTest");
                occupation.occupyVertex(0, tail);
                occupation.occupyVertex(1, head);
                ManagedControlPointIterator iterator = (ManagedControlPointIterator) PathIterator.get(targetGraph, data, tail, head, occupation, () -> 2, settings);
                Path path;
                while ((path = iterator.next()) != null) {
                    assertTrue(toTest.test(new Case(iterator.controlPoints(), occupation, path, iterator.finalPath(), iterator.firstPath())));
                }
            }

        }
    }

    static class Case {

        final List<Integer> controlPoints;
        final GlobalOccupation globalOccupation;
        final Path path;
        final Path finalPath;
        final Path firstPath;

        Case(List<Integer> controlPoints, GlobalOccupation globalOccupation, Path path, Path finalPath, Path firstPath) {
            this.controlPoints = controlPoints;
            this.globalOccupation = globalOccupation;
            this.path = path;
            this.finalPath = finalPath;
            this.firstPath = firstPath;
        }
    }





}
