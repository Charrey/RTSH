package thesis;

import com.charrey.Configuration;
import com.charrey.IsoFinder;
import com.charrey.TestCaseProvider;
import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.result.FailResult;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.SuccessResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.util.Util;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PrunerTestSpace {


    public static void main(String[] args) throws InterruptedException {
        //testSerialZeroDomainLabelDegree("spaceserialzerodomainlabeldegree.txt");
        testCachedZeroDomainLabelDegree("spacecachedzerodomainlabeldegree.txt");
        testParallelZeroDomainLabelDegree("spaceparallelzerodomainlabeldegree.txt");
        testSerialAlldiffLabelDegree("spaceserialalldifflabeldegree.txt");
        testCachedAlldiffLabelDegree("spacecachedalldifflabeldegree.txt");
        testParallelAlldiffLabelDegree("spaceparallelalldifflabeldegree.txt");
        testSerialZeroDomainUnmatchedDegrees("spaceserialzerodomainUnmatchedDegrees.txt");
        testCachedZeroDomainUnmatchedDegrees("spacecachedzerodomainUnmatchedDegrees.txt");
        testParallelZeroDomainUnmatchedDegrees("spaceparallelzerodomainUnmatchedDegrees.txt");
        testSerialAlldiffUnmatched("spaceserialalldiffUnmatchedDegrees.txt");
        testCachedAlldiffUnmatched("spacecachedalldiffUnmatchedDegrees.txt");
        testParallelAlldiffUnmatched("spaceparallelalldiffUnmatchedDegrees.txt");
        testSerialZeroDomainMReach("spaceserialzerodomainMReach.txt");
        testCachedZeroDomainMReach("spacecachedzerodomainMReach.txt");
        testParallelZeroDomainMReach("spaceparallelzerodomainMReach.txt");
        testSerialAlldiffMReach("spaceserialalldiffMReach.txt");
        testCachedAlldiffMReach("spacecachedalldiffMReach.txt");
        testParallelAlldiffMReach("spaceparallelalldiffMReach.txt");
        testSerialZeroDomainNReach("spaceserialzerodomainNReach.txt");
        testCachedZeroDomainNReach("spacecachedzerodomainNReach.txt");
        testParallelZeroDomainNReach("spaceparallelzerodomainNReach.txt");
        testSerialAlldiffNReach("spaceserialalldiffNReach.txt");
        testCachedAlldiffNReach("spacecachedalldiffNReach.txt");
        testParallelAlldiffNReach("spaceparallelalldiffNReach.txt");
    }


    public static void testSerialZeroDomainLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void testSerialZeroDomainUnmatchedDegrees(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testSerialZeroDomainMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
       configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testSerialZeroDomainNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedZeroDomainLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedZeroDomainUnmatchedDegrees(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedZeroDomainMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedZeroDomainNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testParallelZeroDomainLabelDegree(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

            List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testParallelZeroDomainUnmatchedDegrees(String fileName) throws InterruptedException {

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(out, System.out),  fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testParallelZeroDomainMReach(String fileName) throws InterruptedException { //cr

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(out, System.out),  fileName);

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
    public static void testParallelZeroDomainNReach(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {


        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withZeroDomainPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(out, System.out),  fileName);

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
    public static void testSerialAlldiffLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withAllDifferentPruning().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));


        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testSerialAlldiffUnmatched(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testSerialAlldiffMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testSerialAlldiffNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withSerialPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedAlldiffLabelDegree(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedAlldiffUnmatched(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedAlldiffMReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testCachedAlldiffNReach(String fileName) throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withCachedPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            comparitiveTest(configurations, 1.5,
                    (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                    , Util.setOf(out, System.out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void testParallelAlldiffLabelDegree(String fileName) throws InterruptedException { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withLabelDegreeFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(System.out, out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void testParallelAlldiffUnmatched(String fileName) { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withUnmatchedDegreesFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(System.out, out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void testParallelAlldiffMReach(String fileName) { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withMatchedReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(System.out, out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void testParallelAlldiffNReach(String fileName) { //cr
        try(FileWriter fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {

        List<Configuration> configurations = new LinkedList<>();

        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withInplaceDFSRouting().withParallelPruning().withAllDifferentPruning().withNeighbourReachabilityFiltering().get(),
                new SettingsBuilder().withInplaceDFSRouting().get()));

        comparitiveTest(configurations, 1.5,
                (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext()
                , Util.setOf(System.out, out),  fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static final long timeout = 10*60*1000;

    static void comparitiveTest(List<Configuration> configurations,
                                double sizeFactor,
                                TestCaseProvider tcp,
                                Set<Appendable> outputs,
                                String additionalInfo) {
        for (Configuration configuration : configurations) {
            Object fileLock = new Object();
            Random threadRandom = new Random(512);
            List<Integer> x = new ArrayList<>();
            List<Double> prunedComparedToNot = new ArrayList<>();

            int currentX = 4;
            int lastCasesDone = 10;
            while (lastCasesDone > 1) {
                Random perXRandom = new Random(threadRandom.nextLong());
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + configuration + ", x = " + currentX + (prunedComparedToNot.isEmpty() ? "" : ", cases done = " + lastCasesDone + ", last ratio = " + prunedComparedToNot.get(prunedComparedToNot.size() - 1)));
                long timeStartForThisX = System.currentTimeMillis();
                double totalSpaceWith = 0d;
                double totalSpaceWithout = 0d;
                int cases = 0;
                while (System.currentTimeMillis() - timeStartForThisX < timeout && cases < 100) {
                    cases++;
                    long testcaseSeed = perXRandom.nextLong();
                    TestCase tc = tcp.get(currentX, 0, 0, 0, testcaseSeed, false);
                    HomeomorphismResult resultWithPrune;
                    HomeomorphismResult resultWithoutPrune;
                    try {
                        resultWithPrune = testWithoutExpectation(tc, timeout, configuration.getSettingsWithContraction());
                        resultWithoutPrune = testWithoutExpectation(tc, timeout, configuration.getSettingsWithoutContraction());
                    } catch (Exception | Error e) {
                        String error = (additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                        synchronized (fileLock) {
                            outputs.forEach(y -> {
                                try {
                                    y.append(error + "\n");
                                    ((Flushable) y).flush();
                                } catch (IOException e2) {
                                    e.printStackTrace();
                                }
                            });
                        }
                        e.printStackTrace();
                        continue;
                    }
                    if (resultWithoutPrune instanceof FailResult || resultWithPrune instanceof FailResult) {
                        System.out.println(additionalInfo + " " + configuration.toString() + " failed, case="+cases +", test case =" + tc + ", seed="+testcaseSeed);
                    } else if (resultWithPrune instanceof SuccessResult && (configuration.getSettingsWithoutContraction() == null || resultWithoutPrune instanceof SuccessResult)) {
                        totalSpaceWith += resultWithPrune.memory;
                        totalSpaceWithout += resultWithoutPrune.memory;
                    }
                }
                if (totalSpaceWith > 0 && totalSpaceWithout > 0) {
                    prunedComparedToNot.add(totalSpaceWith / totalSpaceWithout);
                    x.add(currentX);
                } else if (totalSpaceWithout == 0) {
                    prunedComparedToNot.add(1d);
                    x.add(currentX);
                }
                lastCasesDone = cases;
                currentX++;
            }
            synchronized (fileLock) {
                outputs.forEach(y -> {
                    try {
                        y.append(configuration.getString(x, prunedComparedToNot) + "\n");
                        ((Flushable) y).flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        }
    }

    @NotNull
    public static HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder(settings).getHomeomorphism(testCase, timeout, "SYSTEMTEST", true);
    }

}
