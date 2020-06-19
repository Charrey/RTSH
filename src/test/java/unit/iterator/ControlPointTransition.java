package unit.iterator;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pathiterators.PathIterator;
import com.charrey.pathiterators.controlpoint.ManagedControlPointIterator;
import com.charrey.runtimecheck.DomainCheckerException;
import com.charrey.settings.PathIterationStrategy;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import com.charrey.util.Util;
import org.jgrapht.Graphs;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlPointTransition {

    private final Random random = new Random(19247);
    private final static int differentGraphSizes = 250;
    private final static int trials = 10;

    private final Settings settings = new Settings(
            true,
            true,
            true,
            RunTimeCheck.NONE,
            PathIterationStrategy.CONTROL_POINT,
            new Random(1823)
    );

    @Test
    @Order(1)
    void TestNoMore() throws DomainCheckerException {
        test(myCase -> {
            OptionalInt counterExample = myCase.globalOccupation.getRoutingOccupied().stream().filter(x -> myCase.path.intermediate().stream().noneMatch(y -> y.data() == x)).findAny();
            if (counterExample.isPresent()) {
                System.err.println("In occupation but not in path: " + counterExample);
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
            Optional<Vertex> counterExample = myCase.finalPath.intermediate().stream().filter(x -> !myCase.globalOccupation.isOccupiedRouting(x)).findAny();
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
            Optional<Vertex> counterExample = myCase.firstPath.intermediate().stream().filter(x -> !myCase.globalOccupation.isOccupiedRouting(x)).findAny();
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
                MyGraph targetGraph = gen.getNext().sourceGraph;
                MyGraph sourceGraph = new MyGraph(true);
                sourceGraph.addEdge(sourceGraph.addVertex(), sourceGraph.addVertex());
                UtilityData data = new UtilityData(sourceGraph, targetGraph);
                Vertex tail = Util.selectRandom(targetGraph.vertexSet(), x -> true, random);
                Vertex head = Util.selectRandom(targetGraph.vertexSet(), x -> x != tail, random);
                counter++;
                System.out.print(counter % 100 == 0 ? counter + "/" + differentGraphSizes * trials + "\n" : "");
                if (Graphs.neighborSetOf(targetGraph, tail).contains(head)) {
                    continue;
                }
                GlobalOccupation occupation = new GlobalOccupation(data, targetGraph.vertexSet().size(), settings);
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

        final List<Vertex> controlPoints;
        final GlobalOccupation globalOccupation;
        final Path path;
        final Path finalPath;
        final Path firstPath;

        Case(List<Vertex> controlPoints, GlobalOccupation globalOccupation, Path path, Path finalPath, Path firstPath) {
            this.controlPoints = controlPoints;
            this.globalOccupation = globalOccupation;
            this.path = path;
            this.finalPath = finalPath;
            this.firstPath = firstPath;
        }
    }





}
