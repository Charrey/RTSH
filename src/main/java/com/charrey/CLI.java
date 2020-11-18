package com.charrey;

import com.charrey.graph.MyEdge;
import com.charrey.graph.MyGraph;
import com.charrey.graph.Path;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.*;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.SourceVertexOrder;
import com.charrey.settings.TargetVertexOrder;
import com.charrey.settings.pruning.PruningMethod;
import com.charrey.settings.pruning.WhenToApply;
import gnu.trove.list.TIntList;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.dot.DOTImporter;
import picocli.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


@SuppressWarnings("CanBeFinal")
@CommandLine.Command(name = "java -jar NDSH.jar",
        exitCodeListHeading = "Exit Codes:%n",
        exitCodeList = {
                " 0:Found homeomorphism",
                "-1:User error occured",
                "-2:No homeomorphism found",
                "-3:The specified timeout period was exceeded",
                "-4:Internal exception occurred"})
public class CLI implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Source graph file (DOT format).")
    private File sourceFile;

    @CommandLine.Parameters(index = "1", description = "Target graph file (DOT format).")
    private File targetFile;

    private final String suffix = "\n(valid options: ${COMPLETION-CANDIDATES})";

    @CommandLine.Option(names = {"-pi", "--pathiterator"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Path iteration method" + suffix)
    PathIteratorName pathIterator = PathIteratorName.DFS;

    @CommandLine.Option(names = {"-c", "--contraction"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Contraction strategy" + suffix)
    ContractionSetting contraction = ContractionSetting.PORTFOLIO;

    @CommandLine.Option(names =  {"-pr", "--pruningmethod"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Pruning method" + suffix)
    PruningMethod pruningMethod = PruningMethod.ALLDIFFERENT;

    @CommandLine.Option(names =  {"-f", "--filtering"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Domain filtering method" + suffix)
    FilteringName filteringName = FilteringName.NREACHABILITY;

    @CommandLine.Option(names =  {"-pra", "--pruningapplication"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Pruning application strategy" + suffix)
    WhenToApply whenToApply = WhenToApply.CACHED;

    @CommandLine.Option(names =  {"-vl", "--vertexLimit"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Maximum number of matching attempts per source graph vertex. A value of 0 means there no such limit.")
    int vertexLimit = 0;

    @CommandLine.Option(names =  {"-pl", "--pathLimit"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Maximum number of matching attempts per source graph edge. A value of 0 means there no such limit.")
    int pathLimit = 0;

    @CommandLine.Option(names =  {"-t", "--timeout"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Timeout value in milliseconds.")
    long timeout = 10*60*1000;

    @CommandLine.Option(names =  {"-svo", "--sourceorder"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Source graph vertex ordering strategy" + suffix)
    SourceVertexOrder sourceVertexOrder = SourceVertexOrder.GREATEST_CONSTRAINED_FIRST;

    @CommandLine.Option(names =  {"-tvo", "--targetorder"}, showDefaultValue = CommandLine.Help.Visibility.ALWAYS, description = "Target graph vertex ordering strategy" + suffix)
    TargetVertexOrder targetVertexOrder = TargetVertexOrder.LARGEST_DEGREE_FIRST;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;



    @Override
    public Integer call() { // business logic
        MyGraph sourceGraph;
        Map<Integer, String> newToOldSource;
        MyGraph targetGraph;
        Map<Integer, String> newToOldTarget;
        try {
            Pair<MyGraph, Map<Integer, String>> sourceGraphPair = loadGraph(sourceFile);
            sourceGraph = sourceGraphPair.getFirst();
            newToOldSource = sourceGraphPair.getSecond();

            Pair<MyGraph, Map<Integer, String>> targetGraphPair = loadGraph(targetFile);
            targetGraph = targetGraphPair.getFirst();
            newToOldTarget = targetGraphPair.getSecond();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
        TestCase testCase = new TestCase(sourceGraph, targetGraph, null, null);
        SettingsBuilder settingsBuilder = new SettingsBuilder();
        setPathIterator(settingsBuilder);
        setSourceVertexOrder(settingsBuilder);
        setTargetVertexOrder(settingsBuilder);
        if (pathLimit > 0) {settingsBuilder.withPathsLimit(pathLimit);}
        if (vertexLimit > 0) {settingsBuilder.withVertexLimit(vertexLimit);}
        setWhenToApply(settingsBuilder);
        setFiltering(settingsBuilder);
        setPruningMethod(settingsBuilder);
        if (contraction == ContractionSetting.ENABLED) {
            settingsBuilder.withContraction();
        } else if (contraction == ContractionSetting.DISABLED) {
            settingsBuilder.withoutContraction();
        }
        Settings settings = settingsBuilder.get();
        HomeomorphismResult result;
        if (contraction == ContractionSetting.PORTFOLIO) {
            result = new PortfolioIsoFinder(new SettingsBuilder(settingsBuilder.get()).withContraction().get(), new SettingsBuilder(settingsBuilder.get()).withoutContraction().get()).getHomeomorphism(testCase, timeout, "", false);
        } else {
            result = new IsoFinder(settings).getHomeomorphism(testCase, timeout, "", false);
        }
        if (result instanceof TimeoutResult) {
            System.err.println("Timeout reached.");
            return -3;
        } else if (result instanceof FailResult || result instanceof CompatibilityFailResult) {
            System.err.println("No homeomorphism found.");
            return -2;
        } else if (result instanceof SuccessResult) {
            int[] vertexPlacement = ((SuccessResult) result).getVertexPlacement();
            System.out.println("----------------------------------------");
            System.out.println("--  source vertex ---> target vertex  --");
            System.out.println("----------------------------------------");
            for (int i = 0; i < vertexPlacement.length; i++) {
                System.out.println(newToOldSource.get(i) + " ---> " + newToOldTarget.get(vertexPlacement[i]));
            }
            System.out.println("----------------------------------------");
            System.out.println("--  source edge   ---> target path    --");
            System.out.println("----------------------------------------");
            Map<MyEdge, Set<Path>> edgePlacement = ((SuccessResult) result).getEdgePlacement();
            edgePlacement.forEach((myEdge, paths) -> System.out.println("(" + newToOldSource.get(myEdge.getSource()) + ", " + newToOldSource.get(myEdge.getTarget()) + ") ---> " + toStrings(newToOldTarget, paths)));
            return 0;
        }
        return -4;
    }

    private String toStrings(Map<Integer, String> newToOldTarget, Set<Path> paths) {
        Set<String> strings = new HashSet<>();
        for (Path path : paths) {
            TIntList list = path.asList();
            StringBuilder sb = new StringBuilder(String.valueOf(newToOldTarget.get(list.get(0))));
            for (int i = 1; i < list.size(); i++) {
                sb.append(" -> ").append(newToOldTarget.get(list.get(i)));
            }
            strings.add(sb.toString());
        }
        return strings.toString();
    }

    private void setPruningMethod(SettingsBuilder settingsBuilder) {
        switch (pruningMethod) {
            case NONE -> settingsBuilder.withoutPruning();
            case ZERODOMAIN -> settingsBuilder.withZeroDomainPruning();
            case ALLDIFFERENT -> settingsBuilder.withAllDifferentPruning();
        }
    }

    private void setFiltering(SettingsBuilder settingsBuilder) {
        switch (filteringName) {
            case LABELDEGREE -> settingsBuilder.withLabelDegreeFiltering();
            case FREENEIGHBOURS -> settingsBuilder.withUnmatchedDegreesFiltering();
            case MREACHABILITY -> settingsBuilder.withMatchedReachabilityFiltering();
            case NREACHABILITY -> settingsBuilder.withNeighbourReachabilityFiltering();
        }
    }

    private void setWhenToApply(SettingsBuilder settingsBuilder) {
        switch (whenToApply) {
            case CACHED -> settingsBuilder.withCachedPruning();
            case SERIAL -> settingsBuilder.withSerialPruning();
            case PARALLEL -> settingsBuilder.withParallelPruning();
        }
    }

    private void setSourceVertexOrder(SettingsBuilder settingsBuilder) {
        switch (sourceVertexOrder) {
            case GREATEST_CONSTRAINED_FIRST -> settingsBuilder.withGreatestConstrainedFirstSourceVertexOrder();
            case RANDOM -> settingsBuilder.withRandomSourceVertexOrder();
        }
    }

    private void setTargetVertexOrder(SettingsBuilder settingsBuilder) {
        switch (targetVertexOrder) {
            case RANDOM -> settingsBuilder.withRandomTargetVertexOrder();
            case CLOSEST_TO_MATCHED -> settingsBuilder.withClosestTargetVertexOrder();
            case CLOSEST_TO_MATCHED_CACHED -> settingsBuilder.withClosestTargetVertexOrderCached();
            case LARGEST_DEGREE_FIRST -> settingsBuilder.withLargestDegreeFirstTargetVertexOrder();
        }
    }

    private void setPathIterator(SettingsBuilder settingsBuilder) {
        switch (pathIterator) {
            case DFS -> settingsBuilder.withInplaceDFSRouting();
            case CP -> settingsBuilder.withControlPointRouting();
            case KPATH -> settingsBuilder.withKPathRouting();
            case GDFS -> settingsBuilder.withCachedGreedyDFSRouting();
            case GDFSAIP -> settingsBuilder.withInplaceNewGreedyDFSRouting();
            case GDFSOIP -> settingsBuilder.withInplaceOldGreedyDFSRouting();
        }
    }

    private Pair<MyGraph, Map<Integer, String>> loadGraph(File sourceFile) throws Exception {
        MyGraph graph = new MyGraph(true);
        Map<Integer, String> reverseNameMap = new HashMap<>();
        try {
            DOTImporter<Integer, MyEdge> importer = new DOTImporter<>();
            final int[] counter = {0};
            importer.setVertexFactory(id -> {
                int toReturn = counter[0]++;
                reverseNameMap.put(toReturn, id);
                return toReturn;
            });
            importer.importGraph(graph, sourceFile);
        } catch (ImportException e) {
            if (e.getCause() instanceof ParseCancellationException) {
                throw new Exception("(" + sourceFile + ") " + e.getMessage());
            }
            throw new Exception(e.getCause().toString());
        }
        return new Pair<>(graph, reverseNameMap);
    }


    public static void main(String[] args) {
        int exitCode = new CommandLine(new CLI()).execute(args);
        System.exit(exitCode);
    }

    public enum PathIteratorName {
        DFS, GDFS, GDFSAIP, GDFSOIP, CP, KPATH
    }

    public enum ContractionSetting {
        ENABLED, DISABLED, PORTFOLIO
    }

    public enum FilteringName {
        LABELDEGREE, FREENEIGHBOURS, MREACHABILITY, NREACHABILITY
    }
}
