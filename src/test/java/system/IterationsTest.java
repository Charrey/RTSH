package system;

import com.charrey.HomeomorphismResult;
import com.charrey.graph.Vertex;
import com.charrey.graph.generation.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.pathiterators.controlpoint.ControlPointIterator;
import com.charrey.settings.PathIterationStrategy;
import com.charrey.settings.RunTimeCheck;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTImporter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class IterationsTest extends SystemTest {

//    private static final long seed = 1924;
//    private static final int minNodes = 10;
//    private static final int maxNodes = 12;
//    private static final Random random = new Random();

//    public static void main(String[] args) throws IOException {
//        random.setSeed(seed + 1);
//        for (int i = 0; i < 100; i++) {
//            int patternNodes = random.nextInt(maxNodes - minNodes) + minNodes;
//            int patternEdges = random.nextInt((patternNodes * (patternNodes - 1))/2);
//            TestCaseGenerator graphGen = new RandomSucceedDirectedTestCaseGenerator(patternNodes, patternEdges, 0.1, 2, seed);
//            graphGen.init(1, false);
//            TestCase testCase = graphGen.getNext();
//            Path directory = Paths.get("performanceTest/directed/" + minNodes + "-" + maxNodes + "/" + i);
//            createDirectoryIfNotExists(directory);
//            DOTExporter<Vertex, DefaultEdge> exporter = new DOTExporter<>();
//            exporter.exportGraph(testCase.sourceGraph,  directory.resolve("source.dot").toFile());
//            exporter.exportGraph(testCase.targetGraph,  directory.resolve("target.dot").toFile());
//            if (!Files.exists(directory.resolve("benchmarks.txt"))) {
//                Files.createFile(directory.resolve("benchmarks.txt"));
//            }
//            if (!Files.exists(directory.resolve("solution.txt"))) {
//                Files.createFile(directory.resolve("solution.txt"));
//            }
//        }
//        for (int i = 0; i < 100; i++) {
//            int patternNodes = random.nextInt(maxNodes - minNodes) + minNodes;
//            int patternEdges = random.nextInt((patternNodes * (patternNodes - 1))/2);
//            TestCaseGenerator graphGen = new RandomSucceedUndirectedTestCaseGenerator(patternNodes, patternEdges, 0.1, 2, seed);
//            graphGen.init(1, false);
//            TestCase testCase = graphGen.getNext();
//            Path directory = Paths.get("performanceTest/undirected/" + minNodes + "-" + maxNodes + "/" + i);
//            createDirectoryIfNotExists(directory);
//            DOTExporter<Vertex, DefaultEdge> exporter = new DOTExporter<>();
//            exporter.exportGraph(testCase.sourceGraph,  directory.resolve("source.dot").toFile());
//            exporter.exportGraph(testCase.targetGraph,  directory.resolve("target.dot").toFile());
//            if (!Files.exists(directory.resolve("benchmarks.txt"))) {
//                Files.createFile(directory.resolve("benchmarks.txt"));
//            }
//            if (!Files.exists(directory.resolve("solution.txt"))) {
//                Files.createFile(directory.resolve("solution.txt"));
//            }
//        }
//    }

//    private static void createDirectoryIfNotExists(Path filename) throws IOException {
//        if (!Files.exists(filename.toAbsolutePath().getParent())) {
//            createDirectoryIfNotExists(filename.toAbsolutePath().getParent());
//        }
//        if (!Files.exists(filename)) {
//            Files.createDirectory(filename);
//        }
//    }

    @SuppressWarnings("ConstantConditions")
    @Test
    @Disabled
    public static void removeBenchmarks() throws IOException {
        File folder = new File("performanceTest/directed");
        for (File category : folder.listFiles()) {
            File[] testCases = category.listFiles();
            for (File testCase : testCases) {
                Files.delete(testCase.toPath().resolve("benchmarks.txt"));
            }
        }
    }


    private final Settings settings = new Settings(
            true,
            true,
            true,
            RunTimeCheck.ALL_DIFFERENT,
            PathIterationStrategy.CONTROL_POINT, new Random(19477));

    @SuppressWarnings("ConstantConditions")
    @Test
    @Order(1)
    void testDirected() throws IOException {
        Logger.getLogger("IsoFinder").setLevel(Level.ALL);
        File folder = new File("performanceTest/directed");
        Map<Path, Long> toWrite = new HashMap<>();
        for (File category : folder.listFiles()) {
            if (!category.getName().equals("6-8")) {
                continue;
            }
            List<File> testCases = Arrays.asList(category.listFiles());
            testCases.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
            for (File testCase : testCases) {
                System.out.println(testCase);
                MyGraph pattern = importDot(testCase.toPath().resolve("source.dot"), true);
                MyGraph target = importDot(testCase.toPath().resolve("target.dot"), true);
                HomeomorphismResult homeomorphism;
                try {
                    homeomorphism = testSucceed(new TestCase(pattern, target),
                            false,
                            1800_000,
                            settings);
                } catch (AssertionError e) {
                    ControlPointIterator.log = true;
                    try {
                        testSucceed(new TestCase(pattern, target),
                                false,
                                1800_000, settings);
                    } catch (AssertionError ignored) {}
                    throw e;
                }
                Path benchmarkPath = testCase.toPath().resolve("benchmarks.txt");
                if (!benchmarkPath.toFile().exists()) {
                    Files.createFile(benchmarkPath);
                }
                if (homeomorphism == null) {
                    toWrite.put(testCase.toPath().resolve("benchmarks.txt"), -1L);
                } else if (!homeomorphism.failed) {
                    exportResult(homeomorphism, testCase.toPath().resolve("solution.txt"));
                    toWrite.put(testCase.toPath().resolve("benchmarks.txt"), homeomorphism.iterations);
                }
            }
        }
        for (Map.Entry<Path, Long> entry : toWrite.entrySet()) {
            Optional<Pair<Settings, Long>> existing = Files.readAllLines(entry.getKey()).stream().map(Settings::readString).filter(x -> x.getFirst().equals(settings)).findAny();
            if (existing.isPresent() && existing.get().getSecond() < entry.getValue()) {
                System.err.println("Performance decreased");
                //System.exit(-1);
            }
        }
        for (Map.Entry<Path, Long> entry : toWrite.entrySet()) {
            exportIterations(entry.getValue(), entry.getKey());
        }

    }

    @SuppressWarnings("ConstantConditions")
    @Test
    @Order(2)
    void analyseSpeeds() throws IOException {
        File folder = new File("performanceTest/directed");
        List<List<Integer>> count = new ArrayList<>();
        List<List<Double>> improvement = new ArrayList<>();
        final Settings baseline =  new Settings(
                true,
                true,
                true,
                RunTimeCheck.ALL_DIFFERENT,
                PathIterationStrategy.CONTROL_POINT,
                new Random(300));
        for (File category : folder.listFiles()) {
            if (!category.getName().equals("6-8")) {
                continue;
            }
            List<File> testCases = Arrays.asList(category.listFiles());
            testCases.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
            for (File testCase : testCases) {
                Path benchmarkPath = testCase.toPath().resolve("benchmarks.txt");
                if (!benchmarkPath.toFile().exists()) {
                    Files.createFile(benchmarkPath);
                }
                List<Pair<Settings, Long>> benchmarks = Files.readAllLines(benchmarkPath).stream().map(Settings::readString).collect(Collectors.toList());
                Optional<Pair<Settings, Long>> foundBaseline =  benchmarks.stream().filter(x -> x.getFirst().equals(baseline)).findAny();
                assert foundBaseline.isPresent() : "You must run with the baseline at least once.";
                double baseLineIterations = foundBaseline.get().getSecond();
                for (Pair<Settings, Long> benchmark : benchmarks.stream().filter(x -> !x.getFirst().equals(baseline)).collect(Collectors.toList())) {
                    int indexWhereItDiffers = getDifference(benchmark.getFirst(), baseline);
                    if (indexWhereItDiffers != -1) {
                        int whatItsValueIs = benchmark.getFirst().getByIndex(indexWhereItDiffers);
                        assert baseline.getByIndex(indexWhereItDiffers) != whatItsValueIs;
                        while (count.size() <= indexWhereItDiffers) {
                            count.add(new ArrayList<>());
                        }
                        while (improvement.size() <= indexWhereItDiffers) {
                            improvement.add(new ArrayList<>());
                        }
                        while (count.get(indexWhereItDiffers).size() <= whatItsValueIs) {
                            count.get(indexWhereItDiffers).add(0);
                        }
                        while (improvement.get(indexWhereItDiffers).size() <= whatItsValueIs) {
                            improvement.get(indexWhereItDiffers).add(1.);
                        }
                        count.get(indexWhereItDiffers).set(whatItsValueIs, count.get(indexWhereItDiffers).get(whatItsValueIs) + 1);
                        assert baseLineIterations != 0.;
                        improvement.get(indexWhereItDiffers).set(whatItsValueIs, improvement.get(indexWhereItDiffers).get(whatItsValueIs) + benchmark.getSecond() / baseLineIterations);
                    }
                }
            }
            for (int option = 0; option < improvement.size(); option++) {
                for (int value = 0; value < improvement.get(option).size(); value++) {
                    if (count.get(option).get(value) == 0) {
                        continue;
                    }
                    improvement.get(option).set(value,  improvement.get(option).get(value) / (double) count.get(option).get(value));
                    System.out.println("Option " + option + " at value " + value + " requires on average " + improvement.get(option).get(value) + " as many iterations compared to baseline.");
                }
            }
            System.out.println();
        }


    }

    private static int getDifference(@NotNull Settings a, @NotNull Settings b) {
        int found = -1;
        if (a.initialLocalizedAllDifferent != b.initialLocalizedAllDifferent) {
            found = 0;
        }
        if (a.initialGlobalAllDifferent != b.initialGlobalAllDifferent) {
            if (found != -1) {
                return -1;
            }
            found = 1;
        }
        if (a.refuseLongerPaths != b.refuseLongerPaths) {
            if (found != -1) {
                return -1;
            }
            found = 2;
        }
        if (a.runTimeCheck != b.runTimeCheck) {
            if (found != -1) {
                return -1;
            }
            found = 3;
        }
        if (a.pathIteration != b.pathIteration) {
            if (found != -1) {
                return -1;
            }
            return 4;
        }
        return found;
    }

    private void exportIterations(long iterations, @NotNull Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
        List<Pair<Settings, Long>> settings = lines.stream().map(Settings::readString).collect(Collectors.toList());
        boolean found = false;
        for (Pair<Settings, Long> line : settings) {
            if (this.settings.equals(line.getFirst())) {
                found = true;
                line.setSecond(iterations);
                break;
            }
        }
        if (!found) {
            settings.add(new Pair<>(this.settings, iterations));
        }
        settings.sort((o1, o2) -> Settings.comparator.compare(o1.getFirst(), o2.getFirst()));
        PrintWriter writer = new PrintWriter(new FileWriter(file.toFile()));
        settings.forEach(x -> writer.println(Settings.writeString(x)));
        writer.close();
    }

    private void exportResult(@NotNull HomeomorphismResult homeomorphism, @NotNull Path resultFile) throws IOException {
        if (resultFile.toFile().length() == 0) {
            Files.write(resultFile, homeomorphism.toString().getBytes());
        }
    }

    @NotNull
    private MyGraph importDot(@NotNull Path resolve, boolean directed) {
        DOTImporter<Vertex, DefaultEdge> importer = new DOTImporter<>();
        MyGraph toImportTo = new MyGraph(directed);
        importer.importGraph(toImportTo, resolve.toFile());
        return toImportTo;
    }


}
