package scriptie;

import com.charrey.Configuration;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static scriptie.Util.comparitiveTest;


public class PruningPerformance {

    @Test
    public void testSerialZeroDomainLabelDegree() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testSerialZeroDomainUnmatchedDegrees() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testSerialZeroDomainMReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testSerialZeroDomainNReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }


    @Test
    public void testCachedZeroDomainLabelDegree() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testCachedZeroDomainUnmatchedDegrees() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testCachedZeroDomainMReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testCachedZeroDomainNReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testParallelZeroDomainLabelDegree() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testParallelZeroDomainUnmatchedDegrees() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testParallelZeroDomainMReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testParallelZeroDomainNReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }


    @Test
    public void testSerialAlldiffLabelDegree() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testSerialAlldiffUnmatched() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testSerialAlldiffMReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testSerialAlldiffNReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withSerialPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }



    @Test
    public void testCachedAlldiffLabelDegree() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testCachedAlldiffUnmatched() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testCachedAlldiffMReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testCachedAlldiffNReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
    }

    @Test
    public void testParallelAlldiffLabelDegree() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);

    }

    @Test
    public void testParallelAlldiffUnmatched() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);

    }

    @Test
    public void testParallelAlldiffMReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);

    }

    @Test
    public void testParallelAlldiffNReach() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   ,
                new SettingsBuilder().withKPathRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       ,
                new SettingsBuilder().withControlPointRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withControlPointRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);
        configurations.clear();
        configurations.add(new Configuration("o",        "purple", "GDFS O IP",
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "magenta", "GDFS A IP",
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   ,
                new SettingsBuilder().withCachedGreedyDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withCachedGreedyDFSRouting().get()));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , 10*60*1000L, true, false);

    }






}
